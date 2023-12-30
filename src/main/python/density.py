from src.main.python.functions import plot_time_series, get_density_simulations, time_series_mean

simulations = 3
dt = 0.0028

dfs = get_density_simulations(simulations=simulations)

d_t = time_series_mean(series=dfs, value_col='density')
#sim0 = dfs[0]
#t = sim0['time']

plot_time_series(t=d_t['time'], y=d_t['density'], x_label=r'$t$ (s)', y_label=r'Densidad: d(t)')

