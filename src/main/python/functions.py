from typing import List, Tuple
import pandas as pd
import math
import matplotlib.pyplot as plt


def manual_window(df: pd.DataFrame, window_size: int) -> Tuple[List[float], List[float]]:
    Q = []
    t = []
    for i in range(0, len(df.index) - window_size, window_size):
        print(f'Caluculando ventana entre {df.iloc[i]["time"]} y {df.iloc[i + window_size]["time"]}')
        Q.append((df.iloc[i + window_size]["escaped"] - df.iloc[i]["escaped"]) / window_size)
        t.append(df.iloc[i]["time"])
    return Q, t


def gtp_window(df: pd.DataFrame, window_size: int) -> pd.DataFrame:

    # Set the time column as the index
    df.set_index('time', inplace=True)
    # Calculate the windowed average of the quotient of "particles" and "time"
    Q = (df['escaped'] / df.index).rolling(window=window_size, min_periods=math.ceil(window_size/4)).mean().reset_index()

    # Reset the index if needed
    df.reset_index(inplace=True)
    Q_t = pd.DataFrame(columns=['time', 'Q'])
    Q_t['time'] = df['time']
    Q_t['Q'] = Q[Q.columns[1]]
    return Q_t


def get_simulations(simulations: int, path: str, types: dict) -> List[pd.DataFrame]:
    dfs = []

    for simulation in range(1, simulations + 1):
        df = pd.read_csv(path.format(simulation=simulation), sep=';')
        df = df.astype(types)
        dfs.append(df)
    return dfs


def get_time_simulations(simulations: int) -> List[pd.DataFrame]:
    path = "/home/abossi/IdeaProjects/sds-final/times_sim={simulation}.csv"
    return get_simulations(simulations=simulations, path=path, types={'time': float, 'escaped': int})


def get_density_simulations(simulations: int) -> List[pd.DataFrame]:
    path = "/home/abossi/IdeaProjects/sds-final/density_sim={simulation}_A=460.00.csv"
    return get_simulations(simulations=simulations, path=path, types={'time': float, 'density': float})


def plot_time_series(t: pd.Series, y: pd.Series, x_label: str, y_label: str) -> None:
    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.plot(t, y, 'o')
    ax.set_xlabel(x_label, size=20)
    ax.set_ylabel(y_label, size=20)
    ax.grid(which="both")
    plt.show()


def calculate_average_exit_time(series: List[pd.DataFrame]) -> Tuple[float, float]:
    # Calculate exit times
    exit_times = [df['time'].iloc[-1] for df in series]

    # Calculate average and std exit time
    average_exit_time = pd.Series(exit_times).mean()
    std_exit_time = pd.Series(exit_times).std()

    return average_exit_time, std_exit_time


def time_series_mean(series: List[pd.DataFrame], value_col: str) -> pd.DataFrame:
    # Create a common time index based on the maximum number of rows in the DataFrames
    common_time_index = pd.concat([df['time'] for df in series]).unique()
    # Align and interpolate each DataFrame on the common time index
    aligned_dfs = [df.set_index('time').reindex(common_time_index).interpolate() for df in series]
    # Calculate the average time series
    average_series = pd.concat([df[value_col] for df in aligned_dfs], axis=1).mean(axis=1)
    # Create the final DataFrame with time and average value
    result_df = pd.DataFrame({'time': common_time_index, value_col: average_series})
    return result_df
