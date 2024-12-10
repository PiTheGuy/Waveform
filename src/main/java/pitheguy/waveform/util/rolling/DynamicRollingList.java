package pitheguy.waveform.util.rolling;

import java.util.function.IntSupplier;

public class DynamicRollingList<T> extends RollingList<T> {
    IntSupplier maxSize;

    public DynamicRollingList(IntSupplier maxSize) {
        super(0);
        this.maxSize = maxSize;
    }

    @Override
    public int maxSize() {
        return maxSize.getAsInt();
    }
}
