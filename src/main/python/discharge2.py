import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
import statistics
import numpy as np

simulations = 3
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

test_df = dfs[0]
test_df.drop(index=test_df.index[0], axis=0, inplace=True)
#test_df.drop(test_df.index[0]).reset_index(drop=True)
test_df['0'] = test_df['0'].astype(int)
test_df['simulation_0'] = test_df['simulation_0'].astype(float)

# descarga
#fig = plt.figure(figsize=(16, 10))
#plt.xlabel(r'$t$ (s)', size=20)
#plt.xlabel(r'n(t)', size=20)
#plt.plot(test_df['simulation_0'], test_df['0'])
#plt.show()


times = test_df['simulation_0'].to_list()
particles = test_df['simulation_0'].to_list()
caudal = []
for i in range(1, len(particles)):
    caudal.append(particles[i] - particles[i-1])

# caudal
plt.xlabel(r'$t$ (s)', size=20)
plt.ylabel(r'q(t)', size=20)
plt.plot(times[1:], caudal)
plt.show()
