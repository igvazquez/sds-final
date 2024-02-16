from src.main.python.functions import plot_time_series, plot_multiple_time_series, get_density_simulations, time_series_mean

decision_type = "decision_availability"
# decision_type = "decision_distance"

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
    dt_list = []

    for idx in range(len(n_list)):
        dfs = get_density_simulations(route_path=user_path+'changeN/'+n_list[idx]+'/'+decision_type, simulations=simulations)
        d_t = time_series_mean(series=dfs, value_col='density')
        dt_list.append([d_t['time'], d_t['density'], d_t['std_error'], n_list[idx]])

    plot_multiple_time_series(data_list=dt_list, x_label=r'$t$ (s)', y_label=r'Densidad: d(t)', filename=user_path+"changeN/plots/"+decision_type+"/d_t")

elif multiple_decision_points:
    
    ''' FOR MULTIPLE DECISION POINTS '''

    point_list = ["0_9L", "0_7L", "0_5L", "0_3L", "0_1L"]
    dt_list = []

    for idx in range(len(point_list)):
        dfs = get_density_simulations(route_path=user_path+'decisionPoint/'+point_list[idx]+'/'+decision_type, simulations=simulations)
        d_t = time_series_mean(series=dfs, value_col='density')
        point = point_list[idx].replace('_', '.')
        dt_list.append([d_t['time'], d_t['density'], d_t['std_error'], point])

    plot_multiple_time_series(data_list=dt_list, x_label=r'$t$ (s)', y_label=r'Densidad: d(t)', filename=user_path+"decisionPoint/plots/"+decision_type+"/d_t")

elif multiple_transactions:

    ''' FOR MULTIPLE TRANSACTION TIMES '''

    tn_list = ["t2", "t4", "t6", "t8", "t10"]
    dt_list = []

    for t_n in tn_list:
        dfs = get_density_simulations(route_path=user_path+'transactionTime/'+t_n+'/'+decision_type, simulations=simulations)
        d_t = time_series_mean(series=dfs, value_col='density')
        dt_list.append([d_t['time'], d_t['density'], d_t['std_error'], t_n])

    plot_multiple_time_series(data_list=dt_list, x_label=r'$t$ (s)', y_label=r'Densidad: d(t)', filename=user_path+"transactionTime/plots/"+decision_type+"/d_t")

else:

    ''' FOR SINGLE TRANSACTION TIME '''
    
    t_n = "t6"
    dfs = get_density_simulations(route_path=user_path+t_n+'/'+decision_type, simulations=simulations)
    d_t = time_series_mean(series=dfs, value_col='density')
    #sim0 = dfs[0]
    #t = sim0['time']
    plot_time_series(t=d_t['time'], y=d_t['density'], std_error=d_t['std_error'], x_label=r'$t$ (s)', y_label=r'Densidad: d(t)', filename=user_path+f"{t_n}/"+decision_type+"/plots/d_t")
