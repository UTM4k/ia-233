import threading
import datetime
import tkinter as tk
from tkinter import messagebox
import winsound

# Таймеры
class TimerTask:
    def __init__(self, action):
        self.action = action

    def run(self):
        self.action()

class Timer:
    def schedule_once(self, task, delay):
        threading.Timer(delay, task.run).start()

    def schedule_at(self, task, run_time):
        now = datetime.datetime.now()
        run_datetime = datetime.datetime.combine(now.date(), run_time)
        if run_datetime < now:
            run_datetime += datetime.timedelta(days=1)
        delay = (run_datetime - now).total_seconds()
        self.schedule_once(task, delay)

    def schedule_periodic(self, task, period, stop_event=None):
        def wrapper():
            if stop_event and stop_event.is_set():
                return
            task.run()
            self.schedule_periodic(task, period, stop_event)
        threading.Timer(period, wrapper).start()


# Общие функции
timer = Timer()

def add_clock(label):
    """Обновление текущего времени на метке каждую секунду"""
    def update_time():
        now = datetime.datetime.now().strftime("%H:%M:%S")
        label.config(text=f"Текущее время: {now}")
        label.after(1000, update_time)
    update_time()


def start_timer1_window():
    win1 = tk.Toplevel()
    win1.title("Таймер 1 - сообщение")
    win1.geometry("800x400")
    win1.lift()
    win1.focus_force()

    clock_label = tk.Label(win1, text="", font=("Arial", 20))
    clock_label.pack(pady=10)
    add_clock(clock_label)

    tk.Label(win1, text="Введите задержку (сек):", font=("Arial", 18)).pack(pady=10)
    entry_delay = tk.Entry(win1, font=("Arial", 16))
    entry_delay.pack(pady=5)

    countdown_label = tk.Label(win1, text="", font=("Arial", 18), fg="blue")
    countdown_label.pack(pady=15)

    result_label = tk.Label(win1, text="", font=("Arial", 20), fg="red")
    result_label.pack(pady=10)

    def start_timer1():
        try:
            delay = int(entry_delay.get())

            def update_countdown(remaining):
                if remaining >= 0:
                    countdown_label.config(text=f"Осталось: {remaining} сек")
                    win1.after(1000, update_countdown, remaining - 1)

            def action():
                result_label.config(text="Время вышло!")

            task = TimerTask(action)
            timer.schedule_once(task, delay)
            update_countdown(delay)

        except ValueError:
            messagebox.showerror("Ошибка", "Введите число секунд!")

    tk.Button(win1, text="Запустить таймер", font=("Arial", 18), command=start_timer1).pack(pady=20)


def start_timer2_window():
    win2 = tk.Toplevel()
    win2.title("Таймер 2 - изменение фона")
    win2.geometry("800x400")
    win2.lift()
    win2.focus_force()

    clock_label = tk.Label(win2, text="", font=("Arial", 20))
    clock_label.pack(pady=10)
    add_clock(clock_label)

    tk.Label(win2, text="Введите время срабатывания (ЧЧ:ММ:СС):", font=("Arial", 18)).pack(pady=20)
    entry_time = tk.Entry(win2, font=("Arial", 16))
    entry_time.pack(pady=10)

    def start_timer2():
        try:
            run_time = datetime.datetime.strptime(entry_time.get(), "%H:%M:%S").time()
            def action():
                win2.config(bg="lightgreen")
            task = TimerTask(action)
            timer.schedule_at(task, run_time)
        except ValueError:
            messagebox.showerror("Ошибка", "Введите время в формате ЧЧ:ММ:СС")

    tk.Button(win2, text="Запустить таймер", font=("Arial", 18), command=start_timer2).pack(pady=20)


def start_timer3_window():
    win3 = tk.Toplevel()
    win3.title("Таймер 3 - звук")
    win3.geometry("800x300")
    win3.lift()
    win3.focus_force()

    clock_label = tk.Label(win3, text="", font=("Arial", 20))
    clock_label.pack(pady=10)
    add_clock(clock_label)

    tk.Label(win3, text="Введите период (сек):", font=("Arial", 18)).pack(pady=20)
    entry_period = tk.Entry(win3, font=("Arial", 16))
    entry_period.pack(pady=10)

    stop_event = threading.Event()

    def start_timer3():
        try:
            period = int(entry_period.get())
            def action():
                winsound.Beep(1000, 500)
            task = TimerTask(action)
            timer.schedule_periodic(task, period, stop_event)
        except ValueError:
            messagebox.showerror("Ошибка", "Введите число секунд!")

    tk.Button(win3, text="Запустить таймер", font=("Arial", 18), command=start_timer3).pack(pady=20)

    def on_close():
        stop_event.set()
        win3.destroy()

    win3.protocol("WM_DELETE_WINDOW", on_close)


# Главное окно
root = tk.Tk()
root.title("Выбор таймера")
root.geometry("600x400")

tk.Label(root, text="Выберите таймер:", font=("Arial", 20)).pack(pady=40)
tk.Button(root, text="Таймер 1 - сообщение", font=("Arial", 18), width=30, command=start_timer1_window).pack(pady=10)
tk.Button(root, text="Таймер 2 - изменение фона", font=("Arial", 18), width=30, command=start_timer2_window).pack(pady=10)
tk.Button(root, text="Таймер 3 - звук", font=("Arial", 18), width=30, command=start_timer3_window).pack(pady=10)

root.mainloop()
