import math
import os
from src.main.python.functions import plot_means, plot_time_series, gtp_window, get_time_simulations, calculate_average_exit_time, time_series_mean, plot_multiple_time_series, plot_escape_time

# asumiendo que tienen un valor de tiempo en cada evento de salida, la formula que tendrian que implementar seria:
# Q(t) = (N(t+5)-N(t))/5, donde t barre todos los tiempos disponibles para hacer la cuenta

# decision_type = "decision_availability"
decision_type = "decision_distance"

''' lucidiaz '''
user_path = "C:/Users/74005/Documents/SDS Final/output/"

''' abossi '''
# user_path = "/home/abossi/IdeaProjects/sds-final/"

simulations = 5
dt = 0.0028

multiple_transactions = False
multiple_decision_points = False
multiple_n_values = True

if multiple_n_values:

    ''' FOR MULTIPLE N VALUES '''

    n_list = ["N100", "N150", "N200", "N250", "N300"]
    nt_list = []
    qt_list = []
    t_list = []

    for idx in range(len(n_list)):

        dfs = get_time_simulations(route_path=user_path+'changeN/'+n_list[idx]+'/'+decision_type+'/old', simulations=simulations)
        window_size = math.ceil(7.5 / dt)  # tamaño de ventana en registros para tiempo de 1s
        
        average_exit_time, std_exit_time = calculate_average_exit_time(series=dfs)
        t_list.append([average_exit_time, std_exit_time, n_list[idx]])
        # print(f'Tiempo promedio de salida: {average_exit_time}. Desvio: {std_exit_time}')
        n_t = time_series_mean(series=dfs, value_col='escaped')
        nt_list.append([n_t['time'], n_t['escaped'], n_t['std_error'], n_list[idx]])
        
        Q = []
        for df in dfs:
            Q.append(gtp_window(df=df, window_size=window_size))
        Q_t = time_series_mean(series=Q, value_col='Q')
        qt_list.append([Q_t['time'], Q_t['Q'], Q_t['std_error'], n_list[idx]])

    dir_path = user_path+"changeN/plots/"+decision_type
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)

    plot_multiple_time_series(data_list=nt_list, x_label=r'$t$ (s)', y_label=r'Descarga: n(t)', filename=user_path+"changeN/plots/old/"+decision_type+"/n_t")
    plot_multiple_time_series(data_list=qt_list, x_label=r'$t$ (s)', y_label=r'Descarga: Q(t)', filename=user_path+"changeN/plots/old/"+decision_type+"/q_t")
    plot_means(series_list=qt_list, filename=user_path+"changeN/plots/old/"+decision_type+"/q_means", xlabel="N", ylabel="Caudal medio <Q(t)> (1/s)")
    plot_escape_time(series_list=t_list, filename=user_path+"changeN/plots/old/"+decision_type+"/escape_time", xlabel="N", ylabel="Tiempo de escape (s)")


elif multiple_decision_points:
    
    ''' FOR MULTIPLE DECISION POINTS '''

    point_list = ["0_9L", "0_7L", "0_5L", "0_3L", "0_1L"]
    nt_list = []
    qt_list = []
    t_list = []

    for idx in range(len(point_list)):

        dfs = get_time_simulations(route_path=user_path+'decisionPoint/'+point_list[idx]+'/'+decision_type+"/old", simulations=simulations)
        window_size = math.ceil(7.5 / dt)  # tamaño de ventana en registros para tiempo de 1s
        
        point = point_list[idx].replace('_', '.')

        average_exit_time, std_exit_time = calculate_average_exit_time(series=dfs)
        t_list.append([average_exit_time, std_exit_time, point_list[idx]])
        # print(f'Tiempo promedio de salida: {average_exit_time}. Desvio: {std_exit_time}')
        n_t = time_series_mean(series=dfs, value_col='escaped')
        nt_list.append([n_t['time'], n_t['escaped'], n_t['std_error'], point])
        
        Q = []
        for df in dfs:
            Q.append(gtp_window(df=df, window_size=window_size))
        Q_t = time_series_mean(series=Q, value_col='Q')
        qt_list.append([Q_t['time'], Q_t['Q'], Q_t['std_error'], point])


    dir_path = user_path+"decisionPoint/plots/"+decision_type
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)

    plot_multiple_time_series(data_list=nt_list, x_label=r'$t$ (s)', y_label=r'Descarga: n(t)', filename=user_path+"decisionPoint/plots/"+decision_type+"/n_t")
    plot_multiple_time_series(data_list=qt_list, x_label=r'$t$ (s)', y_label=r'Descarga: Q(t)', filename=user_path+"decisionPoint/plots/"+decision_type+"/q_t")
    plot_means(series_list=qt_list, filename=user_path+"decisionPoint/plots/"+decision_type+"/q_means", xlabel="Altura de decisión (m)", ylabel="Caudal medio <Q(t)> (1/s)")
    plot_escape_time(series_list=t_list, filename=user_path+"decisionPoint/plots/"+decision_type+"/escape_time", xlabel="Altura de decisión (m)", ylabel="Tiempo de escape(s)")

elif multiple_transactions:

    ''' FOR MULTIPLE TRANSACTION TIMES '''

    tn_list = ["t2", "t4", "t6", "t8", "t10"]
    nt_list = []
    qt_list = []
    t_list = []

    for t_n in tn_list:
        dfs = get_time_simulations(route_path=user_path+'transactionTime/'+t_n+'/'+decision_type, simulations=simulations)
        window_size = math.ceil(7.5 / dt)  # tamaño de ventana en registros para tiempo de 1s

        average_exit_time, std_exit_time = calculate_average_exit_time(series=dfs)
        t_list.append([average_exit_time, std_exit_time, t_n])
        # print(f'Tiempo promedio de salida: {average_exit_time}. Desvio: {std_exit_time}')
        n_t = time_series_mean(series=dfs, value_col='escaped')
        nt_list.append([n_t['time'], n_t['escaped'], n_t['std_error'], t_n])
        
        Q = []
        for df in dfs:
            Q.append(gtp_window(df=df, window_size=window_size))
        Q_t = time_series_mean(series=Q, value_col='Q')
        qt_list.append([Q_t['time'], Q_t['Q'], Q_t['std_error'], t_n])

    dir_path = user_path+"transactionTime/plots/"+decision_type
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)

    plot_multiple_time_series(data_list=nt_list, x_label=r'$t$ (s)', y_label=r'Descarga: n(t)', filename=user_path+"transactionTime/plots/"+decision_type+"/n_t")
    plot_multiple_time_series(data_list=qt_list, x_label=r'$t$ (s)', y_label=r'Descarga: Q(t)', filename=user_path+"transactionTime/plots/"+decision_type+"/q_t")
    plot_means(series_list=qt_list, filename=user_path+"transactionTime/plots/"+decision_type+"/q_means", xlabel="Tiempo de transacción (s)", ylabel="Caudal medio <Q(t)> (1/s)")
    plot_escape_time(series_list=t_list, filename=user_path+"transactionTime/plots/"+decision_type+"/escape_time", xlabel="Tiempo de transacción (s)", ylabel="Tiempo de escape(s)")

else:

    ''' FOR SINGLE TRANSACTION TIME AND SINGLE DECISION POINT '''
    
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
    plot_time_series(t=n_t['time'], y=n_t['escaped'], std_error=n_t['std_error'], x_label=r'$t$ (s)', y_label=r'Descarga: n(t)', filename=user_path+f"{t_n}/"+decision_type+"/plots/n_t")
    plot_time_series(t=Q_t['time'], y=Q_t['Q'], std_error=Q_t['std_error'], x_label=r'$t$ (s)', y_label=r'Descarga: Q(t)', filename=user_path+f"{t_n}/"+decision_type+"/plots/Q_t")
