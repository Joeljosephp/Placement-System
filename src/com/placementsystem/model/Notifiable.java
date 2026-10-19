package com.placementsystem.model;

/**
 * Interface representing an entity capable of receiving notifications.
 * Demonstrates the OOP concept of Abstraction and Interface Contracts.
 */
public interface Notifiable {
    void sendNotification(String message);
}