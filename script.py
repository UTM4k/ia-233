import tkinter as tk
from tkinter import messagebox
import threading
import time


class ThreeTimersApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Три таймера")
        self.root.geometry("1000x650")
        self.root.configure(bg="#e6f2ff")

        self.running = False
        self.counter = 0
        self.timer2_time = None
        self.timer2_text = None
        self.limit = None
        self.start_time = None

        # Счётчик времени сверху (минуты:секунды:миллисекунды)
        self.time_label = tk.Label(root, text="00:00:000", font=("Arial", 20, "bold"), bg="#e6f2ff")
        self.time_label.pack(pady=10)

        # Основная рамка для таймеров
        frame = tk.Frame(root, bg="#e6f2ff")
        frame.pack(expand=True, fill="both", padx=20, pady=10)

        # Создаём три колонки с описанием
        self.timer1_frame = self.create_timer_column(frame, "Таймер 1 (каждые 15 секунд пишет «Предмет Топ»)")
        self.timer2_frame = self.create_timer_column(frame, "Таймер 2 (обратный, пишет ваш текст по времени)")
        self.timer3_frame = self.create_timer_column(frame, "Таймер 3 (основной, считает и останавливает все)")

        self.timer1_frame.grid(row=0, column=0, padx=15, pady=10, sticky="nsew")
        self.timer2_frame.grid(row=0, column=1, padx=15, pady=10, sticky="nsew")
        self.timer3_frame.grid(row=0, column=2, padx=15, pady=10, sticky="nsew")

        # Поля ввода для таймера 2
        self.timer2_time_entry = tk.Entry(self.timer2_frame, font=("Arial", 14), width=30)
        self.timer2_time_entry.pack(pady=5)
        self.timer2_time_entry.insert(0, "Введите время (сек) для таймера 2")

        self.timer2_text_entry = tk.Entry(self.timer2_frame, font=("Arial", 14), width=30)
        self.timer2_text_entry.pack(pady=5)
        self.timer2_text_entry.insert(0, "Введите текст для таймера 2")

        # Поле для таймера 3 (лимит)
        self.entry = tk.Entry(self.timer3_frame, font=("Arial", 14), width=30)
        self.entry.pack(pady=5)
        self.entry.insert(0, "Введите число секунд для остановки")

        # Окно для вывода всех сообщений
        self.log_text = tk.Text(root, height=15, font=("Arial", 12), bg="#f0f8ff")
        self.log_text.pack(padx=20, pady=20, fill="both", expand=True)

        # Кнопки
        self.start_button = tk.Button(root, text="Запустить", command=self.start, font=("Arial", 14),
                                      bg="#99ccff", fg="black", width=15)
        self.start_button.pack(pady=5)

        self.stop_button = tk.Button(root, text="Остановить", command=self.stop, font=("Arial", 14),
                                     bg="#99ccff", fg="black", width=15)
        self.stop_button.pack(pady=5)

    def create_timer_column(self, parent, title):
        frame = tk.Frame(parent, bg="#cce6ff", bd=2, relief="ridge")
        label = tk.Label(frame, text=title, font=("Arial", 14, "bold"), bg="#99ccff", fg="black", wraplength=250)
        label.pack(fill="x", padx=5, pady=5)
        return frame

    def start(self):
        if self.running:
            return

        try:
            self.limit = int(self.entry.get())
            self.timer2_time = int(self.timer2_time_entry.get())
            self.timer2_text = self.timer2_text_entry.get()
        except ValueError:
            messagebox.showerror("Ошибка", "Введите корректные числа")
            return

        self.running = True
        self.counter = 0
        self.start_time = time.time()
        threading.Thread(target=self.run_timers, daemon=True).start()
        self.update_time_label()

    def stop(self):
        self.running = False

    def update_time_label(self):
        if self.running:
            elapsed = time.time() - self.start_time
            minutes = int(elapsed // 60)
            seconds = int(elapsed % 60)
            milliseconds = int((elapsed - int(elapsed)) * 1000)
            self.time_label.config(text=f"{minutes:02d}:{seconds:02d}:{milliseconds:03d}")
            # Обновление каждые 50 мс для плавного отображения
            self.root.after(50, self.update_time_label)

    def run_timers(self):
        while self.running and self.counter < self.limit:
            time.sleep(1)
            self.counter += 1

            # Таймер 3 — вывод в лог каждую секунду
            self.log(f"Таймер 3: {self.counter} сек.")

            # Таймер 1 — каждые 15 секунд
            if self.counter % 15 == 0:
                self.log(f"Таймер 1: Предмет Топ (прошло {self.counter} сек.)")

            # Таймер 2 — срабатывает в указанное время
            if self.counter == self.timer2_time:
                self.log(f"Таймер 2: {self.timer2_text}")

        # При достижении лимита останавливаем таймеры
        if self.counter >= self.limit:
            self.log("Таймер 3: достиг лимита, все остановлено.")
            self.running = False

    def log(self, message):
        # Добавляем сообщение в текстовое окно
        self.log_text.insert(tk.END, message + "\n")
        self.log_text.see(tk.END)


if __name__ == "__main__":
    root = tk.Tk()
    app = ThreeTimersApp(root)
    root.mainloop()
