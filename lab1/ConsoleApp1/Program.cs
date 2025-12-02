using System.Collections.Concurrent;
using System.Timers;

public class TimerInfo
{
    public Guid Id { get; } = Guid.NewGuid();
    public System.Timers.Timer Timer { get; }
    private int inHandler = 0;
    public volatile bool IsStopping = false;
    public bool AutoReset => Timer.AutoReset;

    public TimerInfo(System.Timers.Timer t) => Timer = t ?? throw new ArgumentNullException(nameof(t));

    public bool TryEnterHandler() => Interlocked.Exchange(ref inHandler, 1) == 0;
    public void ExitHandler() => Interlocked.Exchange(ref inHandler, 0);

    public bool WaitForHandlerToFinish(int timeoutMs)
    {
        int waited = 0;
        const int step = 10;
        while (Interlocked.CompareExchange(ref inHandler, 0, 0) == 1 && waited < timeoutMs)
        {
            Thread.Sleep(step);
            waited += step;
        }
        return Interlocked.CompareExchange(ref inHandler, 0, 0) == 0;
    }
}

public sealed class Scheduler : IDisposable
{
    private readonly ConcurrentDictionary<Guid, TimerInfo> activeTimers = new();
    private readonly object disposeLock = new();
    private bool disposed = false;

    public Guid[] ActiveTimerIds => activeTimers.Keys.ToArray();

    public TimerInfo Schedule(TimeSpan delay, ElapsedEventHandler callback)
    {
        if (callback == null) throw new ArgumentNullException(nameof(callback));
        if (delay < TimeSpan.Zero) delay = TimeSpan.Zero;

        var timer = new System.Timers.Timer(delay.TotalMilliseconds) { AutoReset = false, Enabled = true };
        var info = new TimerInfo(timer);
        activeTimers[info.Id] = info;

        ElapsedEventHandler wrapper = null!;
        wrapper = (sender, e) =>
        {
            if (!activeTimers.TryGetValue(info.Id, out var myInfo)) return;
            if (myInfo.IsStopping) return;
            if (!myInfo.TryEnterHandler()) return;

            try
            {
                try { callback(sender, e); } catch (Exception cbEx) { Console.Error.WriteLine($"Callback error: {cbEx}"); }

                myInfo.IsStopping = true;
                try { myInfo.Timer.Stop(); } catch { }
                activeTimers.TryRemove(myInfo.Id, out _);
                try { myInfo.Timer.Dispose(); } catch { }
            }
            finally
            {
                myInfo.ExitHandler();
                timer.Elapsed -= wrapper;
            }
        };

        timer.Elapsed += wrapper;
        timer.Start();
        return info;
    }

    public TimerInfo Schedule(DateTime when, ElapsedEventHandler callback)
    {
        if (callback == null) throw new ArgumentNullException(nameof(callback));
        // Interpret input as Local time and convert to UTC for stable scheduling
        DateTime targetUtc = when.Kind == DateTimeKind.Utc ? when : when.ToUniversalTime();
        var nowUtc = DateTime.UtcNow;
        var ms = (targetUtc - nowUtc).TotalMilliseconds;
        if (ms < 0) ms = 0;
        return Schedule(TimeSpan.FromMilliseconds(ms), callback);
    }

    public TimerInfo Schedule(TimeSpan period, ElapsedEventHandler callback, bool autoReset = true)
    {
        if (callback == null) throw new ArgumentNullException(nameof(callback));
        if (period <= TimeSpan.Zero) throw new ArgumentOutOfRangeException(nameof(period), "Period must be positive.");

        var timer = new System.Timers.Timer(period.TotalMilliseconds) { AutoReset = autoReset, Enabled = true };
        var info = new TimerInfo(timer);
        activeTimers[info.Id] = info;

        ElapsedEventHandler wrapper = null!;
        wrapper = (sender, e) =>
        {
            if (!activeTimers.TryGetValue(info.Id, out var myInfo)) return;
            if (myInfo.IsStopping) return;
            if (!myInfo.TryEnterHandler()) return;

            try
            {
                try { callback(sender, e); } catch (Exception cbEx) { Console.Error.WriteLine($"Periodic callback error: {cbEx}"); }
            }
            finally { myInfo.ExitHandler(); }
        };

        timer.Elapsed += wrapper;
        timer.Start();
        return info;
    }

    public bool TryStopTimer(Guid id, int waitForHandlerMs = 200)
    {
        if (activeTimers.TryGetValue(id, out var info))
        {
            info.IsStopping = true;
            try { info.Timer.Stop(); } catch { }
            info.WaitForHandlerToFinish(waitForHandlerMs);
            try { info.Timer.Dispose(); } catch { }
            return activeTimers.TryRemove(id, out _);
        }
        return false;
    }

    public void StopAll()
    {
        foreach (var kv in activeTimers) kv.Value.IsStopping = true;

        foreach (var kv in activeTimers)
        {
            var id = kv.Key;
            var info = kv.Value;
            try { info.Timer.Stop(); } catch { }
            info.WaitForHandlerToFinish(200);
            try { info.Timer.Dispose(); } catch { }
            activeTimers.TryRemove(id, out _);
        }
    }

    public void Dispose()
    {
        lock (disposeLock)
        {
            if (disposed) return;
            StopAll();
            disposed = true;
        }
    }
}

class Program
{
    static readonly object consoleLock = new();
    static void SafeWriteLine(string s) { lock (consoleLock) Console.WriteLine(s); }

    static void Main()
    {
        SafeWriteLine("Timer Scheduler Console");
        SafeWriteLine("-----------------------");
        SafeWriteLine("Options:");
        SafeWriteLine(" 1 - Schedule a one-shot timer that fires AFTER a delay (e.g. '00:00:05').");
        SafeWriteLine(" 2 - Schedule a periodic timer with a PERIOD (e.g. '00:00:02'.");
        SafeWriteLine(" 3 - Schedule a one-shot timer that fires AT a DateTime (e.g. '2025-10-01 14:30:00' or '2025-10-01T14:30:00').");
        SafeWriteLine("Commands: 'stop' - stop all timers; 'list' - list active timers; 'exit' - stop all and exit.");
        SafeWriteLine("");

        using var scheduler = new Scheduler();

        while (true)
        {
            Console.Write("Choose option (1/2/3) or command: ");
            var input = Console.ReadLine();
            if (input == null) continue;

            // simple commands
            switch (input.Trim().ToLowerInvariant())
            {
                case "stop":
                    scheduler.StopAll();
                    SafeWriteLine("--> All timers stopped.");
                    continue;
                case "list":
                    var ids = scheduler.ActiveTimerIds;
                    SafeWriteLine($"Active timers: {ids.Length}");
                    foreach (var id in ids) SafeWriteLine($"  {id}");
                    continue;
                case "exit":
                    scheduler.StopAll();
                    SafeWriteLine("Exiting. Goodbye.");
                    return;
            }

            // Main menu options
            switch (input.Trim())
            {
                case "1":
                    // one-shot after delay
                    Console.Write("Enter delay (TimeSpan like 'hh:mm:ss'): ");
                    var delayText = Console.ReadLine();
                    if (!TryParseTimeSpanOrSeconds(delayText, out TimeSpan delay))
                    {
                        SafeWriteLine("--> Invalid delay format.");
                        break;
                    }

                    var info1 = scheduler.Schedule(delay, (s, e) =>
                    {
                        SafeWriteLine($"\n[One-shot after {delay} fired at {e.SignalTime:HH:mm:ss.fff}]");
                    });
                    SafeWriteLine($"--> One-shot timer scheduled (Id: {info1.Id}).");
                    break;

                case "2":
                    // periodic
                    Console.Write("Enter period (TimeSpan like 'hh:mm:ss'): ");
                    var periodText = Console.ReadLine();
                    if (!TryParseTimeSpanOrSeconds(periodText, out TimeSpan period))
                    {
                        SafeWriteLine("--> Invalid period format.");
                        break;
                    }

                    var info2 = scheduler.Schedule(period, (s, e) =>
                    {
                        SafeWriteLine($"\n[Periodic {period} timer event at {e.SignalTime:HH:mm:ss.fff}]");
                    }, autoReset: true);
                    SafeWriteLine($"--> Periodic timer scheduled (Id: {info2.Id}).");
                    break;

                case "3":
                    // one-shot at a specific datetime
                    Console.Write("Enter DateTime (local) (examples: '2025-10-01 14:30:00' or '2025-10-01T14:30:00'): ");
                    var dtText = Console.ReadLine();
                    if (!TryParseLocalDateTime(dtText, out DateTime whenLocal))
                    {
                        SafeWriteLine("--> Invalid DateTime format.");
                        break;
                    }

                    var info3 = scheduler.Schedule(whenLocal, (s, e) =>
                    {
                        SafeWriteLine($"\n[One-shot at {whenLocal} (local) fired at {e.SignalTime:HH:mm:ss.fff}]");
                    });
                    SafeWriteLine($"--> One-shot-at-DateTime scheduled (Id: {info3.Id}, fires at local {whenLocal}).");
                    break;

                default:
                    SafeWriteLine("--> Unknown option/command.");
                    break;
            }
        }
    }

    static bool TryParseTimeSpanOrSeconds(string? text, out TimeSpan result)
    {
        result = default;
        if (string.IsNullOrWhiteSpace(text)) return false;
        text = text.Trim();
        if (TimeSpan.TryParse(text, out result)) return true;
        return false;
    }

    static bool TryParseLocalDateTime(string? text, out DateTime local)
    {
        local = default;
        if (string.IsNullOrWhiteSpace(text)) return false;
        text = text.Trim();
        // try flexible parsing
        if (DateTime.TryParse(text, out var dt))
        {
            // If dt.Kind is Unspecified, treat as Local
            if (dt.Kind == DateTimeKind.Utc) local = dt.ToLocalTime();
            else local = DateTime.SpecifyKind(dt, DateTimeKind.Local);
            return true;
        }
        return false;
    }
}
