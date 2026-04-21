#!/usr/bin/env python3
"""
Genera graph.json y edge_geometry.json a partir del GeoJSON del campus.

Lógica:
1. Extraer todos los nodos (Points con type="node") y sus coordenadas
2. Para cada LineString, encontrar el nodo más cercano al PRIMER punto
   y el nodo más cercano al ÚLTIMO punto de la línea
3. Solo crear una arista si ambos extremos coinciden exactamente con un nodo
   (tolerancia < 1 metro)
4. El costo de cada arista es la distancia Haversine entre los nodos extremos
5. Las geometrías son las coordenadas exactas de la LineString

Esto garantiza que NO se crean líneas imaginarias: cada arista del grafo
corresponde a una LineString real del mapa.
"""

import json
import math
import sys
from collections import defaultdict

def haversine(lat1, lon1, lat2, lon2):
    """Distancia en metros entre dos puntos geográficos."""
    R = 6371000  # Radio de la Tierra en metros
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlam = math.radians(lon2 - lon1)
    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlam/2)**2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))


def find_nearest_node(lon, lat, nodes, max_dist=3.0):
    """
    Encuentra el nodo más cercano a un punto dado.
    Retorna (node_id, distancia) o (None, inf) si ninguno está a menos de max_dist metros.
    """
    best_id = None
    best_dist = float('inf')
    for nid, (nlat, nlon) in nodes.items():
        d = haversine(lat, lon, nlat, nlon)
        if d < best_dist:
            best_dist = d
            best_id = nid
    if best_dist <= max_dist:
        return best_id, best_dist
    return None, best_dist


def main():
    geojson_path = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/assets/campus_updated.geojson"

    with open(geojson_path, 'r') as f:
        data = json.load(f)

    features = data['features']

    # --- Paso 1: Extraer nodos ---
    nodes = {}  # id -> (lat, lon)
    poi_nodes = set()

    for feat in features:
        geom = feat.get('geometry', {})
        props = feat.get('properties', {})

        if geom.get('type') == 'Point' and props.get('type') == 'node':
            node_id = str(feat.get('id', ''))
            if not node_id:
                continue
            coords = geom['coordinates']  # [lon, lat]
            lon, lat = coords[0], coords[1]
            nodes[node_id] = (lat, lon)

            if props.get('isPoi') or props.get('nodeKind') == 'poi':
                poi_nodes.add(node_id)

    print(f"Nodos encontrados: {len(nodes)}")
    print(f"POIs encontrados: {len(poi_nodes)}")

    # --- Paso 2: Extraer aristas de LineStrings ---
    graph = defaultdict(dict)  # node_id -> {neighbor_id: cost}
    edge_geometry = {}  # "startId|endId" -> [[lon, lat], ...]

    linestrings_total = 0
    linestrings_matched = 0
    linestrings_skipped_self = 0
    linestrings_skipped_no_match = 0
    linestrings_skipped_degenerate = 0

    for feat in features:
        geom = feat.get('geometry', {})
        if geom.get('type') != 'LineString':
            continue

        linestrings_total += 1
        coords = geom['coordinates']  # lista de [lon, lat]

        if len(coords) < 2:
            linestrings_skipped_degenerate += 1
            continue

        # Verificar que no sea una línea degenerada (inicio = fin exacto)
        start_lon, start_lat = coords[0][0], coords[0][1]
        end_lon, end_lat = coords[-1][0], coords[-1][1]

        if abs(start_lon - end_lon) < 1e-10 and abs(start_lat - end_lat) < 1e-10:
            linestrings_skipped_degenerate += 1
            continue

        # Encontrar nodos más cercanos a los extremos
        start_node, start_dist = find_nearest_node(start_lon, start_lat, nodes, max_dist=3.0)
        end_node, end_dist = find_nearest_node(end_lon, end_lat, nodes, max_dist=3.0)

        if start_node is None or end_node is None:
            linestrings_skipped_no_match += 1
            continue

        if start_node == end_node:
            linestrings_skipped_self += 1
            continue

        linestrings_matched += 1

        # Calcular costo como distancia real de caminata (suma de segmentos)
        cost = sum(
            haversine(coords[i][1], coords[i][0], coords[i+1][1], coords[i+1][0])
            for i in range(len(coords) - 1)
        )

        # Solo guardar si es la primera vez o si esta línea es más corta
        key_fwd = f"{start_node}|{end_node}"
        key_rev = f"{end_node}|{start_node}"

        if end_node not in graph[start_node] or cost < graph[start_node][end_node]:
            graph[start_node][end_node] = cost
            graph[end_node][start_node] = cost

            # Geometría forward
            edge_geometry[key_fwd] = coords
            # Geometría reverse
            edge_geometry[key_rev] = list(reversed(coords))

    print(f"\nLineStrings totales: {linestrings_total}")
    print(f"  Matched (aristas válidas): {linestrings_matched}")
    print(f"  Skipped (degeneradas): {linestrings_skipped_degenerate}")
    print(f"  Skipped (self-loop): {linestrings_skipped_self}")
    print(f"  Skipped (sin nodo cercano): {linestrings_skipped_no_match}")

    # --- Paso 3: Verificar conectividad ---
    unique_edges = set()
    for node_id, neighbors in graph.items():
        for neighbor_id in neighbors:
            edge = tuple(sorted([node_id, neighbor_id]))
            unique_edges.add(edge)

    print(f"\nAristas únicas en el grafo: {len(unique_edges)}")
    print(f"Nodos con conexiones: {len(graph)}")

    # Verificar nodos aislados
    isolated = [nid for nid in nodes if nid not in graph]
    if isolated:
        print(f"\n⚠️  Nodos aislados (sin conexiones): {len(isolated)}")
        for nid in isolated[:10]:
            label = " (POI)" if nid in poi_nodes else ""
            print(f"   - {nid}{label}")
        if len(isolated) > 10:
            print(f"   ... y {len(isolated) - 10} más")

    # Verificar componentes conexas con BFS
    visited = set()
    components = []
    for nid in graph:
        if nid in visited:
            continue
        component = set()
        queue = [nid]
        while queue:
            current = queue.pop(0)
            if current in visited:
                continue
            visited.add(current)
            component.add(current)
            for neighbor in graph[current]:
                if neighbor not in visited:
                    queue.append(neighbor)
        components.append(component)

    print(f"\nComponentes conexas: {len(components)}")
    for i, comp in enumerate(sorted(components, key=len, reverse=True)):
        pois_in = len(comp & poi_nodes)
        print(f"  Componente {i+1}: {len(comp)} nodos ({pois_in} POIs)")

    # --- Paso 4: Generar graph.json ---
    graph_json = {}
    for node_id, neighbors in sorted(graph.items()):
        edges = []
        for neighbor_id, cost in sorted(neighbors.items()):
            edges.append({"id": neighbor_id, "cost": round(cost, 2)})
        graph_json[node_id] = edges

    output_graph = "app/src/main/assets/graph.json"
    with open(output_graph, 'w') as f:
        json.dump(graph_json, f, separators=(',', ':'))
    print(f"\n✅ graph.json generado: {output_graph}")

    # --- Paso 5: Generar edge_geometry.json ---
    output_edge = "app/src/main/assets/edge_geometry.json"
    with open(output_edge, 'w') as f:
        json.dump(edge_geometry, f, separators=(',', ':'))
    print(f"✅ edge_geometry.json generado: {output_edge}")

    print(f"\n📊 Resumen final:")
    print(f"   Nodos: {len(nodes)} ({len(poi_nodes)} POIs)")
    print(f"   Aristas: {len(unique_edges)}")
    print(f"   Entradas edge_geometry: {len(edge_geometry)}")


if __name__ == '__main__':
    main()
