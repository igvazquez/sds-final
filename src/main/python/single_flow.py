import math
import os
from src.main.python.functions import plot_time_series, gtp_window, get_time_simulations, calculate_average_exit_time, time_series_mean, plot_multiple_time_series

# asumiendo que tienen un valor de tiempo en cada evento de salida, la formula que tendrian que implementar seria:
# Q(t) = (N(t+5)-N(t))/5, donde t barre todos los tiempos disponibles para hacer la cuenta

user_path = "C:/Users/74005/Documents/SDS Final/output/transactionTime/"
# user_path = "/home/abossi/IdeaProjects/sds-final/"

simulations = 5
dt = 0.0028

multiple = True

if multiple:

    ''' FOR MULTIPLE TRANSACTION TIMES '''

    tn_list = ["t2", "t4", "t6", "t8", "t10"]
    nt_list = []
    qt_list = []

    for t_n in tn_list:
        dfs = get_time_simulations(route_path=user_path+t_n+'/decision_distance', simulations=simulations)
        window_size = math.ceil(7.5 / dt)  # tamaño de ventana en registros para tiempo de 1s

        average_exit_time, std_exit_time = calculate_average_exit_time(series=dfs)
        print(f'Tiempo promedio de salida: {average_exit_time}. Desvio: {std_exit_time}')
        n_t = time_series_mean(series=dfs, value_col='escaped')
        nt_list.append([n_t['time'], n_t['escaped'], n_t['std_error'], t_n])
        
        Q = []
        for df in dfs:
            Q.append(gtp_window(df=df, window_size=window_size))
        Q_t = time_series_mean(series=Q, value_col='Q')
        qt_list.append([Q_t['time'], Q_t['Q'], Q_t['std_error'], t_n])


    dir_path = user_path+"plots"
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)

    plot_multiple_time_series(data_list=nt_list, x_label=r'$t$ (s)', y_label=r'Descarga: n(t)', filename=user_path+"plots/decision_distance/n_t")
    plot_multiple_time_series(data_list=qt_list, x_label=r'$t$ (s)', y_label=r'Descarga: Q(t)', filename=user_path+"plots/decision_distance/q_t")

else:

    ''' FOR SINGLE TRANSACTION TIME '''
    
    t_n = "t6"
    dfs = get_time_simulations(route_path=user_path+t_n, simulations=simulations)
    window_size = math.ceil(7.5 / dt)  # tamaño de ventana en registros para tiempo de 1s

    average_exit_time, std_exit_time = calculate_average_exit_time(series=dfs)
    print(f'Tiempo promedio de salida: {average_exit_time}. Desvio: {std_exit_time}')
    n_t = time_series_mean(series=dfs, value_col='escaped')
    Q = []
    for df in dfs:
        Q.append(gtp_window(df=df, window_size=window_size))

    Q_t = time_series_mean(series=Q, value_col='Q')
    dir_path = user_path+f"{t_n}/plots"
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)
    plot_time_series(t=n_t['time'], y=n_t['escaped'], std_error=n_t['std_error'], x_label=r'$t$ (s)', y_label=r'Descarga: n(t)', filename=user_path+f"{t_n}/decision_distance/plots/n_t")
    plot_time_series(t=Q_t['time'], y=Q_t['Q'], std_error=Q_t['std_error'], x_label=r'$t$ (s)', y_label=r'Descarga: Q(t)', filename=user_path+f"{t_n}/decision_distance/plots/Q_t")
