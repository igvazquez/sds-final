import math
import numpy as np
import pandas as pd
from scipy.stats import sem
from typing import List, Tuple
import matplotlib.pyplot as plt


def manual_window(df: pd.DataFrame, window_size: int) -> Tuple[List[float], List[float]]:
    Q = []
    t = []
    for i in range(0, len(df.index) - window_size, window_size):
        print(f'Calculando ventana entre {df.iloc[i]["time"]} y {df.iloc[i + window_size]["time"]}')
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


def get_time_simulations(route_path: str, simulations: int) -> List[pd.DataFrame]:
    route_path += "/times_sim={simulation}.csv"
    return get_simulations(simulations=simulations, path=route_path, types={'time': float, 'escaped': int})


def get_density_simulations(route_path: str, simulations: int) -> List[pd.DataFrame]:
    route_path += "/density_sim={simulation}_A=460,00.csv"
    return get_simulations(simulations=simulations, path=route_path, types={'time': float, 'density': float})


def plot_time_series(t: pd.Series, y: pd.Series, std_error: pd.Series, x_label: str, y_label: str, filename: str) -> None:
    fig = plt.figure(figsize=(30, 30))
    ax = fig.add_subplot(1, 1, 1)
    ax.plot(t, y, '-')
    ax.fill_between(t, y - std_error, y + std_error, alpha=0.2)
    ax.set_xlabel(x_label, size=20)
    ax.set_ylabel(y_label, size=20)
    ax.grid(which="both")
    ax.tick_params(axis='both', which='major', labelsize=15)  # Increase axis values size
    plt.xticks(np.arange(min(t), max(t)+1, 10))  # Add ticks every 10 seconds
    plt.tight_layout(pad=1.5)  # Adjust padding
    plt.savefig(f'{filename}.png')


def plot_multiple_time_series(data_list: List[Tuple[pd.Series, pd.Series, pd.Series, str]], x_label: str, y_label: str, filename: str) -> None:
    fig = plt.figure(figsize=(30, 10))
    ax = fig.add_subplot(1, 1, 1)

    for t, y, std_error, label in data_list:
        ax.plot(t, y, '-', label=label)
        ax.fill_between(t, y - std_error, y + std_error, alpha=0.2)

    ax.set_xlabel(x_label, size=20)
    ax.set_ylabel(y_label, size=20)
    ax.grid(which="both")
    ax.tick_params(axis='both', which='major', labelsize=15)  # Increase axis values size
    plt.xticks(np.arange(min(t), max(t)+1, 10))  # Add ticks every 10 seconds
    plt.tight_layout(pad=1.5)  # Adjust padding
    ax.legend(fontsize=16, loc="upper right")  # Add a legend
    plt.savefig(f'{filename}.png')

def plot_escape_time(series_list: List[Tuple[float, float, str]], filename: str, xlabel: str, ylabel: str) -> None:
    fig = plt.figure()
    for value, std, label in series_list:
        plt.errorbar(label, value, yerr=std, fmt='o')
    fig.axes[0].set_xlabel(xlabel)
    fig.axes[0].set_ylabel(ylabel)
    fig.axes[0].grid(axis="y")
    fig.axes[0].set_ylim([0, 120])
    plt.savefig(f'{filename}.png')

def plot_means(series_list: List[Tuple[pd.Series, pd.Series, pd.Series, str]], filename: str, xlabel: str, ylabel: str) -> None:
    fig = plt.figure()
    for _, series, _, label in series_list:
        mean = series.mean()
        std = series.std()
        plt.errorbar(label, mean, yerr=std, fmt='o')
    fig.axes[0].set_xlabel(xlabel)
    fig.axes[0].set_ylabel(ylabel)
    fig.axes[0].grid(axis="y")
    plt.yticks(np.arange(0,7,0.5))
    plt.savefig(f'{filename}.png')

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
    # Concatenate the value columns of the aligned DataFrames once
    values = pd.concat([df[value_col] for df in aligned_dfs], axis=1)
    # Calculate the average time series and standard error using numpy and scipy functions
    average_series = np.mean(values, axis=1)
    std_error = sem(values, axis=1, nan_policy='omit')
    # Create the final DataFrame with time, average value, and standard error
    result_df = pd.DataFrame({'time': common_time_index, value_col: average_series, 'std_error': std_error})
    return result_df
