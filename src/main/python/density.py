import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("density_sims=2_A=130.00.csv")
simulations = 2

dfs = []

idx = 0
for i in range(simulations-1):
    sim = 'simulation_' + str(i+1)
    start = idx
    idx = df[df['simulation_0'] == sim].index.to_list()[0]
    dfs.append(df[start:idx])

dfs.append(df[idx:])


for data in dfs:
    x = list(range(data.size))
    plt.scatter(x, data)
    plt.title('Density')

plt.show()
