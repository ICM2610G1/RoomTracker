#!/usr/bin/env python3
"""
Verify southern campus routes after graph topology fix.
Simulates A* pathfinding using the same Haversine cost model as GraphLoader.kt.
"""

import json
import math
import heapq
from collections import defaultdict

# ── File paths ────────────────────────────────────────────────────────────────
GRAPH_PATH       = "/Users/juanfelipegutierrez/Documents/2026/Movil/Proyecto Room tracker/RoomTracker/app/src/main/assets/graph.json"
GEOJSON_PATH     = "/Users/juanfelipegutierrez/Documents/2026/Movil/Proyecto Room tracker/RoomTracker/app/src/main/assets/campus_updated.geojson"
EDGE_GEOM_PATH   = "/Users/juanfelipegutierrez/Documents/2026/Movil/Proyecto Room tracker/RoomTracker/app/src/main/assets/edge_geometry.json"

# ── Haversine distance (metres) ───────────────────────────────────────────────
def haversine(lat1, lon1, lat2, lon2):
    R = 6_371_000.0
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi  = math.radians(lat2 - lat1)
    dlam  = math.radians(lon2 - lon1)
    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlam/2)**2
    return 2 * R * math.asin(math.sqrt(a))

# ── Load node coordinates from GeoJSON ────────────────────────────────────────
def load_coords(path):
    with open(path, encoding="utf-8") as f:
        gj = json.load(f)
    coords = {}
    for feat in gj["features"]:
        if feat.get("geometry", {}).get("type") == "Point":
            node_id = str(feat["id"])
            lon, lat = feat["geometry"]["coordinates"]
            coords[node_id] = (lat, lon)
    return coords

# ── Load graph and recalculate costs as Haversine distances ───────────────────
def load_graph(graph_path, coords):
    with open(graph_path, encoding="utf-8") as f:
        raw = json.load(f)

    graph = defaultdict(list)   # node_id -> [(neighbour, distance_m)]
    missing_coords = set()

    for src, neighbours in raw.items():
        for nb in neighbours:
            dst = str(nb["id"])
            # Recalculate cost as real Haversine distance (mirrors GraphLoader.kt)
            if src in coords and dst in coords:
                lat1, lon1 = coords[src]
                lat2, lon2 = coords[dst]
                dist = haversine(lat1, lon1, lat2, lon2)
            else:
                # Fall back to stored cost if either node has no coords
                dist = float(nb["cost"])
                if src not in coords: missing_coords.add(src)
                if dst not in coords: missing_coords.add(dst)
            graph[src].append((dst, dist))

    if missing_coords:
        print(f"[WARN] Nodes missing coords (using stored cost): {missing_coords}\n")
    return graph

# ── A* pathfinding ─────────────────────────────────────────────────────────────
def astar(graph, coords, src, dst):
    """Returns (path, total_distance_m) or (None, inf) if unreachable."""
    def h(node):
        if node in coords and dst in coords:
            la1, lo1 = coords[node]
            la2, lo2 = coords[dst]
            return haversine(la1, lo1, la2, lo2)
        return 0.0

    open_set = [(0.0 + h(src), 0.0, src, [src])]
    visited   = {}

    while open_set:
        f, g, node, path = heapq.heappop(open_set)
        if node in visited and visited[node] <= g:
            continue
        visited[node] = g
        if node == dst:
            return path, g
        for nb, w in graph.get(node, []):
            ng = g + w
            if nb not in visited or visited[nb] > ng:
                heapq.heappush(open_set, (ng + h(nb), ng, nb, path + [nb]))

    return None, math.inf

# ── Load edge geometry ─────────────────────────────────────────────────────────
def load_edge_geometry(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)

def edge_geometry_key(a, b):
    return f"{a}|{b}"

def polyline_length(pts):
    total = 0.0
    for i in range(len(pts) - 1):
        lon1, lat1 = pts[i]
        lon2, lat2 = pts[i+1]
        total += haversine(lat1, lon1, lat2, lon2)
    return total

# ── Analyse one route ──────────────────────────────────────────────────────────
def analyse_route(label, src, dst, graph, coords, edge_geom):
    path, total_dist = astar(graph, coords, str(src), str(dst))

    print(f"\n{'='*70}")
    print(f"ROUTE: {label}  ({src} → {dst})")
    print(f"{'='*70}")

    if path is None:
        print("  !! UNREACHABLE !!")
        return

    print(f"  Node sequence ({len(path)} nodes): {' → '.join(path)}")
    print(f"  Total distance: {total_dist:.2f} m")

    # ── Check latitude sanity ────────────────────────────────────────────────
    suspicious = [n for n in path if n in coords and coords[n][0] < 4.6255]
    if suspicious:
        print(f"  [FLAG] Nodes with lat < 4.6255 (suspicious, outside campus?): {suspicious}")
    else:
        print(f"  [OK]   All nodes have lat >= 4.6255 (within expected campus bounds)")

    # ── Check straight-line edges with distance > 30 m ───────────────────────
    flagged_edges = []
    for i in range(len(path) - 1):
        a, b = path[i], path[i+1]
        key = edge_geometry_key(a, b)
        geom = edge_geom.get(key)
        if geom is not None and len(geom) == 2:
            seg_len = polyline_length(geom)
            if seg_len > 30.0:
                flagged_edges.append((a, b, seg_len))

    if flagged_edges:
        print(f"  [FLAG] Edges with only 2-point geometry AND length > 30 m:")
        for a, b, d in flagged_edges:
            print(f"         {a} → {b}  ({d:.1f} m)  — possible straight-line shortcut")
    else:
        print(f"  [OK]   No suspicious 2-point straight edges > 30 m found")

    # ── Per-edge breakdown ───────────────────────────────────────────────────
    print(f"\n  Edge-by-edge breakdown:")
    cumulative = 0.0
    for i in range(len(path) - 1):
        a, b = path[i], path[i+1]
        # Haversine distance for this hop
        if a in coords and b in coords:
            la1, lo1 = coords[a]
            la2, lo2 = coords[b]
            d = haversine(la1, lo1, la2, lo2)
        else:
            d = 0.0
        cumulative += d
        key = edge_geometry_key(a, b)
        geom = edge_geom.get(key)
        n_pts = len(geom) if geom else 0
        print(f"    {a:>20} → {b:<20}  {d:7.2f} m  (geom pts: {n_pts})  cumulative: {cumulative:.2f} m")

# ── Main ───────────────────────────────────────────────────────────────────────
def main():
    print("Loading data files...")
    coords    = load_coords(GEOJSON_PATH)
    graph     = load_graph(GRAPH_PATH, coords)
    edge_geom = load_edge_geometry(EDGE_GEOM_PATH)

    print(f"  Loaded {len(coords)} node coordinates")
    print(f"  Loaded {len(graph)} graph nodes")
    print(f"  Loaded {len(edge_geom)} edge geometry entries")

    # ── Print southern campus node coordinates for orientation ───────────────
    southern_ids = ["1", "12", "29", "30", "32", "163", "478", "479", "480", "481", "482"]
    print("\nSouthern campus node coordinates:")
    for nid in southern_ids:
        c = coords.get(nid, "NOT FOUND")
        if c != "NOT FOUND":
            lat, lon = c
            print(f"  Node {nid:>5}: lat={lat:.6f}, lon={lon:.6f}")
        else:
            print(f"  Node {nid:>5}: NOT FOUND in GeoJSON")

    # ── Routes to verify ─────────────────────────────────────────────────────
    routes = [
        ("Southernmost→Northern hub",         480,  1),
        ("South→Major hub",                    12, 163),
        ("Very south→North",                   30,  1),
        ("479→480 (should go via 478…482)",   479, 480),
        ("Short south route",                  29, 30),
        ("South→North",                        32,  1),
    ]

    for label, src, dst in routes:
        analyse_route(label, src, dst, graph, coords, edge_geom)

    # ── Special focus: 479→480 shortest path ─────────────────────────────────
    print(f"\n{'='*70}")
    print("SPECIAL CHECK: Shortest path 479 → 480")
    print(f"{'='*70}")
    path479, dist479 = astar(graph, coords, "479", "480")
    if path479 is None:
        print("  !! UNREACHABLE !!")
    else:
        print(f"  Path:     {' → '.join(path479)}")
        print(f"  Distance: {dist479:.2f} m")
        # Check if there is a direct edge 479→480 in the graph
        direct = any(nb == "480" for nb, _ in graph.get("479", []))
        print(f"  Direct edge 479→480 exists in graph: {direct}")
        if direct:
            direct_dist = next(d for nb, d in graph["479"] if nb == "480")
            print(f"  Direct edge cost: {direct_dist:.2f} m")
            if len(path479) > 2:
                print(f"  [OK]  A* correctly chose the LONGER multi-hop path over the direct edge.")
                print(f"        (Direct edge cost {direct_dist:.2f} m > routed path {dist479:.2f} m is impossible if A* is correct)")
            else:
                print(f"  [WARN] A* took the direct edge — is this intended?")
        else:
            print(f"  [OK]  No direct 479→480 edge; route goes via intermediate nodes as expected.")

    # ── Neighbourhood of nodes 479 and 480 ───────────────────────────────────
    print(f"\n{'='*70}")
    print("NEIGHBOURHOOD CHECK: direct neighbours of 479 and 480")
    print(f"{'='*70}")
    for nid in ("479", "480"):
        neighbours = graph.get(nid, [])
        print(f"\n  Node {nid} neighbours ({len(neighbours)}):")
        for nb, d in sorted(neighbours, key=lambda x: x[1]):
            print(f"    → {nb:<20}  {d:.2f} m")

    print("\n\nDone.")

if __name__ == "__main__":
    main()
