package com.xuanma.module;

public interface LockingPlateListener {
    void OnCallback(int index, LockingStatus stat);
    public enum LockingStatus {
        SUCCESS(1), FAIL(2), NoAddr(3), USELESS(4);
        private int val;
        private LockingStatus(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }
}
