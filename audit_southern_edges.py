#!/usr/bin/env python3
"""
Audit script for southern campus edges in graph.json.

Data formats discovered:
- GeoJSON: Point features have top-level "id" field (e.g. "29", "n_xxx")
- graph.json: { "nodeId": [ {"id": "neighborId", "cost": float}, ... ], ... }
- edge_geometry.json: { "A|B": [ [lon,lat], ... ], ... }  (pipe-delimited key)
"""

import json
import math

BASE = "/Users/juanfelipegutierrez/Documents/2026/Movil/Proyecto Room tracker/RoomTracker/app/src/main/assets"
GRAPH_PATH = f"{BASE}/graph.json"
GEOJSON_PATH = f"{BASE}/campus_updated.geojson"
EDGE_GEOM_PATH = f"{BASE}/edge_geometry.json"

SOUTHERN_LAT = 4.6275
VERY_SOUTHERN_LAT = 4.6265

SPECIAL_NODES = {"29", "30", "31", "32", "390", "395", "478", "479", "480", "481", "482", "483"}


def haversine(lat1, lon1, lat2, lon2):
    R = 6371000
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return 2 * R * math.asin(math.sqrt(a))


# ── Load node coordinates from GeoJSON (top-level "id" field) ───────────────
with open(GEOJSON_PATH, encoding="utf-8") as f:
    geojson = json.load(f)

nodes = {}  # str id -> {"lat": ..., "lon": ...}
for feature in geojson["features"]:
    if feature["geometry"]["type"] == "Point":
        fid = feature.get("id")
        if fid is not None:
            lon, lat = feature["geometry"]["coordinates"][:2]
            nodes[str(fid)] = {"lat": lat, "lon": lon}

print(f"Loaded {len(nodes)} Point nodes from GeoJSON")

# ── Load graph ───────────────────────────────────────────────────────────────
with open(GRAPH_PATH, encoding="utf-8") as f:
    graph = json.load(f)

print(f"Loaded graph with {len(graph)} nodes")

# ── Load edge geometry ───────────────────────────────────────────────────────
with open(EDGE_GEOM_PATH, encoding="utf-8") as f:
    edge_geom = json.load(f)

print(f"Loaded edge_geometry with {len(edge_geom)} entries")

# ── Parse graph edges (deduplicated) ────────────────────────────────────────
# graph[nodeA] = [ {"id": nodeB, "cost": w}, ... ]
seen_edges = set()
edges = []  # list of (str_a, str_b)

for node_a_str, neighbors in graph.items():
    if not isinstance(neighbors, list):
        continue
    for nb in neighbors:
        node_b_str = str(nb["id"])
        key = tuple(sorted([node_a_str, node_b_str]))
        if key not in seen_edges:
            seen_edges.add(key)
            edges.append((node_a_str, node_b_str))

print(f"Total unique edges: {len(edges)}")

# ── Helper: get geometry point count for an edge ────────────────────────────
def get_geom_point_count(a, b):
    """Return number of points in edge geometry, or None if not found."""
    geom = edge_geom.get(f"{a}|{b}") or edge_geom.get(f"{b}|{a}")
    if geom is None:
        return None
    if isinstance(geom, list):
        return len(geom)
    return None


# ── Analyse edges ────────────────────────────────────────────────────────────
southern_straight = []
very_southern_edges = []

for a, b in edges:
    coord_a = nodes.get(a)
    coord_b = nodes.get(b)
    if coord_a is None or coord_b is None:
        continue

    lat_a, lon_a = coord_a["lat"], coord_a["lon"]
    lat_b, lon_b = coord_b["lat"], coord_b["lon"]

    dist = haversine(lat_a, lon_a, lat_b, lon_b)
    pts = get_geom_point_count(a, b)
    # None means no custom geometry entry => straight line; 2 pts also straight
    is_straight = (pts is None) or (pts <= 2)

    both_south = (lat_a < SOUTHERN_LAT) and (lat_b < SOUTHERN_LAT)
    any_very_south = (lat_a < VERY_SOUTHERN_LAT) or (lat_b < VERY_SOUTHERN_LAT)

    record = {
        "a": a, "lat_a": lat_a, "lon_a": lon_a,
        "b": b, "lat_b": lat_b, "lon_b": lon_b,
        "dist": dist, "pts": pts, "is_straight": is_straight,
        "both_south": both_south,
    }

    if both_south and is_straight:
        southern_straight.append(record)

    if any_very_south and is_straight:
        very_southern_edges.append(record)


# ── Report 1: southern straight-line edges ───────────────────────────────────
southern_straight.sort(key=lambda r: r["dist"], reverse=True)

print("\n" + "=" * 100)
print(f"SECTION 1: SOUTHERN STRAIGHT-LINE EDGES  (both endpoints lat < {SOUTHERN_LAT})")
print(f"Total: {len(southern_straight)}")
print("=" * 100)
print(f"{'Edge A':>6} -> {'B':<6}  {'Dist(m)':>8}  {'Pts':>6}  {'Lat_A':>10}  {'Lon_A':>12}  {'Lat_B':>10}  {'Lon_B':>12}")
print("-" * 100)
for r in southern_straight:
    pts_str = str(r["pts"]) if r["pts"] is not None else "no-geom"
    print(f"  {r['a']:>5} -> {r['b']:<6}  {r['dist']:>8.1f}  {pts_str:>6}  {r['lat_a']:>10.6f}  {r['lon_a']:>12.6f}  {r['lat_b']:>10.6f}  {r['lon_b']:>12.6f}")


# ── Report 2: very southern edges ────────────────────────────────────────────
very_southern_edges.sort(key=lambda r: min(r["lat_a"], r["lat_b"]))

print("\n" + "=" * 110)
print(f"SECTION 2: VERY SOUTHERN STRAIGHT-LINE EDGES  (at least one endpoint lat < {VERY_SOUTHERN_LAT})")
print(f"Total: {len(very_southern_edges)}")
print("=" * 110)
print(f"{'Edge A':>6} -> {'B':<6}  {'Dist(m)':>8}  {'Pts':>6}  {'Lat_A':>10}  {'Lon_A':>12}  {'Lat_B':>10}  {'Lon_B':>12}  Note")
print("-" * 110)
for r in very_southern_edges:
    pts_str = str(r["pts"]) if r["pts"] is not None else "no-geom"
    if r["lat_a"] < VERY_SOUTHERN_LAT and r["lat_b"] < VERY_SOUTHERN_LAT:
        note = "BOTH very south"
    elif r["lat_a"] < VERY_SOUTHERN_LAT:
        note = f"A very south | B lat={r['lat_b']:.6f}"
    else:
        note = f"B very south | A lat={r['lat_a']:.6f}"
    print(f"  {r['a']:>5} -> {r['b']:<6}  {r['dist']:>8.1f}  {pts_str:>6}  {r['lat_a']:>10.6f}  {r['lon_a']:>12.6f}  {r['lat_b']:>10.6f}  {r['lon_b']:>12.6f}  {note}")


# ── Report 3: edges that CROSS the lat 4.6265 boundary ───────────────────────
crossing = []
for a, b in edges:
    coord_a = nodes.get(a)
    coord_b = nodes.get(b)
    if coord_a is None or coord_b is None:
        continue
    lat_a, lon_a = coord_a["lat"], coord_a["lon"]
    lat_b, lon_b = coord_b["lat"], coord_b["lon"]
    pts = get_geom_point_count(a, b)
    is_straight = (pts is None) or (pts <= 2)
    if not is_straight:
        continue
    # One side above, one side below VERY_SOUTHERN_LAT => crosses the boundary
    if (lat_a < VERY_SOUTHERN_LAT) != (lat_b < VERY_SOUTHERN_LAT):
        dist = haversine(lat_a, lon_a, lat_b, lon_b)
        crossing.append({
            "a": a, "lat_a": lat_a, "lon_a": lon_a,
            "b": b, "lat_b": lat_b, "lon_b": lon_b,
            "dist": dist, "pts": pts,
        })

crossing.sort(key=lambda r: r["dist"], reverse=True)
print("\n" + "=" * 110)
print(f"SECTION 3: STRAIGHT-LINE EDGES CROSSING lat={VERY_SOUTHERN_LAT} BOUNDARY (one side above, one below)")
print(f"Total: {len(crossing)}")
print("=" * 110)
print(f"{'Edge A':>6} -> {'B':<6}  {'Dist(m)':>8}  {'Pts':>6}  {'Lat_A':>10}  {'Lon_A':>12}  {'Lat_B':>10}  {'Lon_B':>12}")
print("-" * 110)
for r in crossing:
    pts_str = str(r["pts"]) if r["pts"] is not None else "no-geom"
    print(f"  {r['a']:>5} -> {r['b']:<6}  {r['dist']:>8.1f}  {pts_str:>6}  {r['lat_a']:>10.6f}  {r['lon_a']:>12.6f}  {r['lat_b']:>10.6f}  {r['lon_b']:>12.6f}")


# ── Report 4: special nodes ───────────────────────────────────────────────────
print("\n" + "=" * 100)
print(f"SECTION 4: SPECIAL NODES CONNECTIONS: {sorted(SPECIAL_NODES, key=lambda x: (len(x), x))}")
print("=" * 100)

# Build full adjacency map (both directions)
adjacency = {}
for node_a_str, neighbors in graph.items():
    if not isinstance(neighbors, list):
        continue
    if node_a_str not in adjacency:
        adjacency[node_a_str] = {}
    for nb in neighbors:
        node_b_str = str(nb["id"])
        adjacency[node_a_str][node_b_str] = nb["cost"]

for sn in sorted(SPECIAL_NODES, key=lambda x: (len(x), x)):
    coord = nodes.get(sn)
    if coord is None:
        print(f"\nNode {sn}: NOT FOUND in GeoJSON")
        continue
    lat, lon = coord["lat"], coord["lon"]

    neighbors_out = adjacency.get(sn, {})
    neighbors_in = {k: v[sn] for k, v in adjacency.items() if sn in v and k != sn}
    all_neighbors = set(neighbors_out.keys()) | set(neighbors_in.keys())

    zone = "VERY SOUTH" if lat < VERY_SOUTHERN_LAT else ("SOUTH" if lat < SOUTHERN_LAT else "NORTH")
    print(f"\nNode {sn} | lat={lat:.6f}  lon={lon:.6f} | Zone: {zone} | Degree: {len(all_neighbors)}")
    print(f"  {'Neighbor':>8}  {'Direction':>10}  {'Dist(m)':>8}  {'Pts':>7}  {'Type':>12}  {'Nb lat':>10}  {'Nb lon':>12}  Nb zone")
    print(f"  {'-'*8}  {'-'*10}  {'-'*8}  {'-'*7}  {'-'*12}  {'-'*10}  {'-'*12}  {'-'*10}")

    for nb in sorted(all_neighbors, key=lambda x: (len(x), x)):
        nb_coord = nodes.get(nb)
        if nb_coord is None:
            print(f"  {nb:>8}  (neighbor NOT in GeoJSON)")
            continue
        nb_lat, nb_lon = nb_coord["lat"], nb_coord["lon"]
        dist = haversine(lat, lon, nb_lat, nb_lon)
        pts = get_geom_point_count(sn, nb)
        pts_str = str(pts) if pts is not None else "no-geom"
        is_straight = (pts is None) or (pts <= 2)
        edge_type = "STRAIGHT" if is_straight else "real-path"

        if nb in neighbors_out and nb in neighbors_in:
            direction = "bidir"
        elif nb in neighbors_out:
            direction = "outgoing"
        else:
            direction = "incoming"

        nb_zone = "verySouth" if nb_lat < VERY_SOUTHERN_LAT else ("south" if nb_lat < SOUTHERN_LAT else "north")
        print(f"  {nb:>8}  {direction:>10}  {dist:>8.1f}  {pts_str:>7}  {edge_type:>12}  {nb_lat:>10.6f}  {nb_lon:>12.6f}  {nb_zone}")

# ── Summary stats ─────────────────────────────────────────────────────────────
print("\n" + "=" * 100)
print("SUMMARY STATISTICS")
print("=" * 100)
total_south_edges = sum(
    1 for a, b in edges
    if nodes.get(a) and nodes.get(b)
    and nodes[a]["lat"] < SOUTHERN_LAT
    and nodes[b]["lat"] < SOUTHERN_LAT
)
total_south_real = sum(
    1 for a, b in edges
    if nodes.get(a) and nodes.get(b)
    and nodes[a]["lat"] < SOUTHERN_LAT
    and nodes[b]["lat"] < SOUTHERN_LAT
    and (get_geom_point_count(a, b) or 0) > 2
)
print(f"Total edges in southern zone (both pts lat < {SOUTHERN_LAT}): {total_south_edges}")
print(f"  - with real path geometry (>2 pts): {total_south_real}")
print(f"  - straight-line (<=2 pts or no geometry): {total_south_edges - total_south_real}")
print(f"Edges crossing lat={VERY_SOUTHERN_LAT} boundary (straight): {len(crossing)}")

print("\nDone.")
