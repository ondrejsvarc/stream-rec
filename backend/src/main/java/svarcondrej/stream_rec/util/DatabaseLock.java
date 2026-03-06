package svarcondrej.stream_rec.util;

import java.util.concurrent.locks.ReentrantLock;

public class DatabaseLock {
    public static final ReentrantLock WRITE_LOCK = new ReentrantLock(true);
}
