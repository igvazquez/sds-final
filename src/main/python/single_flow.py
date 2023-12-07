import math
from typing import List, Tuple
import pandas as pd
import matplotlib.pyplot as plt


# asumiendo que tienen un valor de tiempo en cada evento de salida, la formula que tendrian que implementar seria:

# Q(t) = (N(t+5)-N(t))/5, donde t barre todos los tiempos disponibles para hacer la cuenta
def manual_window(df: pd.DataFrame, window_size: int) -> Tuple[List[float], List[float]]:
    Q = []
    t = []
    for i in range(0, len(df.index) - window_size, window_size):
        print(f'Caluculando ventana entre {df.iloc[i]["time"]} y {df.iloc[i + window_size]["time"]}')
        Q.append((df.iloc[i + window_size]["escaped"] - df.iloc[i]["escaped"]) / window_size)
        t.append(df.iloc[i]["time"])
    return Q, t


def get_simulations(simulations: int) -> List[pd.DataFrame]:
    dfs = []

    for simulation in range(1, simulations + 1):
        path = f"/home/abossi/IdeaProjects/sds-final/times_sim={simulation}.csv"
        df = pd.read_csv(path, sep=';')
        dfs.append(df)
    return dfs


# esta parte la dejo igual porque lo estoy tomando del archivo de multiples
simulations = 1
dt = 0.0028
min_particles = 50

dfs = get_simulations(simulations=simulations)

# a partir de aca asumo que es uno solo
sim0 = dfs[0]


# Define the fixed time window (e.g., 5 seconds)
window_size = math.ceil(1 / dt)  # tamaño de ventana en registros para tiempo de 1s
# Calculate the windowed average of the quotient of "particles" and "time"
Q, t = manual_window(df=sim0, window_size=window_size)


fig = plt.figure(figsize=(16, 10))
ax = fig.add_subplot(1, 1, 1)
ax.plot(sim0['time'], sim0['escaped'], 'o')
ax.set_xlabel(r'$t$ (s)', size=20)
ax.set_ylabel(r'Descarga: n(t)', size=20)
ax.grid(which="both")
ax.tick_params(axis='both', which='major', labelsize=18)


fig = plt.figure(figsize=(16, 10))
ax = fig.add_subplot(1, 1, 1)
ax.plot(t, Q, 'o')
ax.set_xlabel(r'$t$ (s)', size=20)
ax.set_ylabel(r'Q(t)', size=20)
ax.grid(which="both")
plt.show()
