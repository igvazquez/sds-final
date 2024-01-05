from src.main.python.functions import plot_time_series, plot_multiple_time_series, get_density_simulations, time_series_mean

user_path = "C:/Users/74005/Documents/SDS Final/output/transactionTime/"
# user_path = "/home/abossi/IdeaProjects/sds-final/"

simulations = 5
dt = 0.0028

multiple = True

if multiple:

    ''' FOR MULTIPLE TRANSACTION TIMES '''

    tn_list = ["t2", "t4", "t6", "t8", "t10"]
    dt_list = []

    for t_n in tn_list:
        dfs = get_density_simulations(route_path=user_path+t_n+'/decision_distance', simulations=simulations)
        d_t = time_series_mean(series=dfs, value_col='density')
        dt_list.append([d_t['time'], d_t['density'], d_t['std_error'], t_n])

    plot_multiple_time_series(data_list=dt_list, x_label=r'$t$ (s)', y_label=r'Densidad: d(t)', filename=user_path+"plots/decision_distance/d_t")

else:

    ''' FOR SINGLE TRANSACTION TIME '''
    
    t_n = "t6"
    dfs = get_density_simulations(route_path=user_path+t_n+'/decision_distance', simulations=simulations)
    d_t = time_series_mean(series=dfs, value_col='density')
    #sim0 = dfs[0]
    #t = sim0['time']
    plot_time_series(t=d_t['time'], y=d_t['density'], std_error=d_t['std_error'], x_label=r'$t$ (s)', y_label=r'Densidad: d(t)', filename=user_path+f"{t_n}/decision_distance/plots/d_t")
