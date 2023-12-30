import math
from src.main.python.functions import plot_time_series, gtp_window, get_time_simulations, calculate_average_exit_time, \
    time_series_mean

# asumiendo que tienen un valor de tiempo en cada evento de salida, la formula que tendrian que implementar seria:

# Q(t) = (N(t+5)-N(t))/5, donde t barre todos los tiempos disponibles para hacer la cuenta

# esta parte la dejo igual porque lo estoy tomando del archivo de multiples
simulations = 3
dt = 0.0028

dfs = get_time_simulations(simulations=simulations)
window_size = math.ceil(7.5 / dt)  # tamaño de ventana en registros para tiempo de 1s

average_exit_time, std_exit_time = calculate_average_exit_time(series=dfs)
print(f'Tiempo promedio de salida: {average_exit_time}. Desvio: {std_exit_time}')
n_t = time_series_mean(series=dfs, value_col='escaped')
Q = []
for df in dfs:
    Q.append(gtp_window(df=df, window_size=window_size))

Q_t = time_series_mean(series=Q, value_col='Q')

plot_time_series(t=n_t['time'], y=n_t['escaped'], x_label=r'$t$ (s)', y_label=r'Descarga: n(t)')
plot_time_series(t=Q_t['time'], y=Q_t['Q'], x_label=r'$t$ (s)', y_label=r'Descarga: n(t)')


# a partir de aca asumo que es uno solo
#sim0 = dfs[0]
#t = sim0['time']
# Calculate the windowed average of the quotient of "particles" and "time"
#Q = pd.Series(data=gtp_window(df=sim0, window_size=window_size))

#plot_time_series(t=t, y=sim0['escaped'], x_label=r'$t$ (s)', y_label=r'Descarga: n(t)')



