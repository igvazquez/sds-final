import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
import statistics
import numpy as np
from numpy.lib.stride_tricks import sliding_window_view


#TODO: ver cuantos decimales usar y como para toda metrica

simulations = 1
#matplotlib.use('Agg')
dt = 0.0028
df = pd.read_csv(f"/home/abossi/IdeaProjects/sds-final/times_sims={simulations}.csv", sep=';')
min_particles = 50
dfs = []

idx = 0
for i in range(simulations-1):
    sim = 'simulation_' + str(i+1)
    start = idx
    idx = df[df['simulation_0'] == sim].index.to_list()[0]
    dfs.append(df[start:idx])

dfs.append(df[idx:])

times = []
for i in range(len(dfs)):
    if i != 0:
        dfs[i].drop(index=dfs[i].index[0], axis=0, inplace=True)
    # data = dfs[i][f'simulation_0']
    times.append(dfs[i].tail(1)['simulation_0'].values[0])

for df in dfs:
    df['0'] = df['0'].astype(int)
    df['simulation_0'] = df['simulation_0'].astype(float)
times = [float(i) for i in times]

#t_avg = sum(times) / float(len(times))
#t_std = statistics.stdev(times)
#print(f'TIEMPO PROMEDIO DE SALIDA DE {simulations} SIMULACIONES: {t_avg}')
#print(f'DESVIO DE LOS TIEMPOS: {t_std}')

min = min([len(x) for x in dfs])
dfs = [df[0:min] for df in dfs]

# hasta aca
times = []
for i in range(min_particles):
    times.append(df[df['0'] == i])

#mean = [np.mean(t['simulation_0']) for t in times]
std = [np.std(t['simulation_0']) for t in times]


np_array = np.array(list(map(lambda x: x.to_numpy(), dfs)))

t = 0
Q = []
T = []
ns = []
# hasta aca parece razonable
for i in range(np_array.shape[1]):
    a = np_array[:, i]
    ns.append(np.sum(a, 0) / len(a))


print('hasta aca')
ns = np.array(ns)
#W = min_particles
#window = sliding_window_view(ns[:, 1], W)
for i in range(1, len(ns)):
    Q.append((ns[i-1] - ns[i]) / dt)

fig = plt.figure(figsize=(16, 10))
ax = fig.add_subplot(1, 1, 1)
#ax.errorbar(mean, range(min_particles) ,xerr=std, capsize=2)
ax.set_xlabel(r'$t$ (s)', size=20)
ax.set_ylabel(r'n(t)', size=20)
ax.grid(which="both")

fig = plt.figure(figsize=(16, 10))
ax = fig.add_subplot(1, 1, 1)
ax.plot(times, Q, 'o')
ax.set_xlabel(r'$t$ (s)', size=20)
ax.set_ylabel(r'Q(t)', size=20)
ax.grid(which="both")
plt.show()




#fig = plt.figure(figsize=(16, 10))
#ax = fig.add_subplot(1, 1, 1)
#ax.set_xlabel(r'$t$ (s)', size=20)
#ax.set_ylabel(r'Descarga: n(t)', size=20)
#ax.grid(which="both")
#ax.tick_params(axis='both', which='major', labelsize=18)

#times = []

#for data in dfs:
#    ax.plot(data.iloc[:, 0], data.iloc[:, 1])
#    times.append(data.iloc[:, 1].iloc[-1])

#t_avg = sum(times) / float(len(times))
#t_std = statistics.stdev(times)
#print(f'TIEMPO PROMEDIO DE SALIDA DE {simulations} SIMULACIONES: {t_avg}')
#print(f'DESVIO DE LOS TIEMPOS: {t_std}')
#plt.savefig("output_plot.png")
