package com.bit.examsystem.teacher.service.listener;

@FunctionalInterface
public interface SubmissionListener {
    /**
     * Called whenever the list of submissions changes.
     */
    void onSubmissionReceived();
}