import csv
import matplotlib.pyplot as plt
from collections import defaultdict

# ==========================
# LECTURE STRONG SCALING
# ==========================

strong_data = defaultdict(lambda: {"workers": [], "times": []})

with open("strong_scaling.csv", newline="") as f:
    reader = csv.DictReader(f)
    for row in reader:
        ntot = int(row["ntot"])
        workers = int(row["workers"])
        time = float(row["time_ms"])

        strong_data[ntot]["workers"].append(workers)
        strong_data[ntot]["times"].append(time)

# ==========================
# GRAPHE STRONG SCALING
# ==========================

plt.figure()

for ntot in sorted(strong_data.keys()):
    workers = strong_data[ntot]["workers"]
    times = strong_data[ntot]["times"]

    T1 = times[0]
    speedup = [T1 / t for t in times]

    plt.plot(workers, speedup, marker='o', label=f"Ntot = {ntot}")

# ligne idéale
workers_ref = sorted(strong_data[list(strong_data.keys())[0]]["workers"])
plt.plot(workers_ref, workers_ref, linestyle='--', label="Idéal")

plt.xlabel("Nombre de threads")
plt.ylabel("Speedup S(p)")
plt.title("Scalabilité forte – Monte Carlo Pi")
plt.legend()
plt.grid(True)

# ==========================
# LECTURE WEAK SCALING
# ==========================

workers_weak = []
times_weak = []

with open("weak_scaling.csv", newline="") as f:
    reader = csv.DictReader(f)
    for row in reader:
        workers_weak.append(int(row["workers"]))
        times_weak.append(float(row["time_ms"]))

# ==========================
# GRAPHE WEAK SCALING
# ==========================

plt.figure()

plt.plot(workers_weak, times_weak, marker='o')
plt.axhline(times_weak[0], linestyle='--', label="Temps idéal (constant)")

plt.xlabel("Nombre de threads")
plt.ylabel("Temps (ms)")
plt.title("Scalabilité faible – Monte Carlo Pi")
plt.legend()
plt.grid(True)

plt.show()
