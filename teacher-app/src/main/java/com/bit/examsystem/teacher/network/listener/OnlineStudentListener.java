package com.bit.examsystem.teacher.network.listener;

/**
 * A listener interface for receiving updates about the online student list.
 */
@FunctionalInterface
public interface OnlineStudentListener {
    /**
     * Called whenever the list of online students changes (connect or disconnect).
     */
    void onStudentListChanged();
}
