package org.example.subscriptioncapacityallocator.exceptions;

public class InvalidAllocationInputException extends RuntimeException{
    public InvalidAllocationInputException(String message){
        super(message);
    }
}
