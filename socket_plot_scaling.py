import csv
import matplotlib.pyplot as plt
from collections import defaultdict
import os


# ==========================
# LECTURE STRONG SCALING
# ==========================

strong_data = defaultdict(lambda: {"workers": [], "times": []})

with open("src/socket_strong_scaling.csv", newline="") as f:
    reader = csv.DictReader(f)
    for row in reader:
        ntot = int(row["ntot"])
        workers = int(row["workers"])
        time = float(row["time_ms"])

        strong_data[ntot]["workers"].append(workers)
        strong_data[ntot]["times"].append(time)

if len(strong_data) == 0:
    print("ERREUR : fichier strong vide")
    exit()

# ==========================
# GRAPHE STRONG SCALING
# ==========================

plt.figure()

for ntot in sorted(strong_data.keys()):
    workers = strong_data[ntot]["workers"]
    times = strong_data[ntot]["times"]
    workers, times = zip(*sorted(zip(workers, times)))

    T1 = times[0]
    speedup = [T1 / t for t in times]

    plt.plot(workers, speedup, marker='o', label=f"Ntot = {ntot}")


workers_ref = sorted(workers)
plt.plot(workers_ref, workers_ref, linestyle='--', label="Idéal")

plt.xlabel("Nombre de workers")
plt.ylabel("Speedup S(p)")
plt.title("Scalabilité forte – MasterSocket Monte Carlo Pi")
plt.legend()
plt.grid(True)

# ==========================
# LECTURE WEAK SCALING CSV
# ==========================
workers_weak = []
times_weak = []

with open("src/socket_weak_scaling.csv", newline="") as f:
    reader = csv.DictReader(f)
    for row in reader:
        workers_weak.append(int(row["workers"]))
        times_weak.append(float(row["time_ms"]))

# ==========================
# TRI PAR NOMBRE DE WORKERS
# ==========================
workers_weak, times_weak = zip(*sorted(zip(workers_weak, times_weak)))

# ==========================
# CALCUL DU SPEEDUP
# ==========================
T1 = times_weak[0]  # temps du premier worker
speedup = [T1 / t for t in times_weak]

# ==========================
# GRAPHE SCALABILITÉ FAIBLE
# ==========================
plt.figure(figsize=(8,5))

plt.plot(workers_weak, speedup, marker='o', label="Speedup réel")
plt.axhline(1.0, color='red', linestyle='--', label="Speedup idéal = 1 worker")

plt.xlabel("Nombre de workers")
plt.ylabel("Speedup S(p)")
plt.title("Scalabilité faible – MasterSocket Monte Carlo Pi")
plt.xticks(workers_weak)
plt.ylim(0, 1.2)
plt.legend()
plt.grid(True)
plt.show()