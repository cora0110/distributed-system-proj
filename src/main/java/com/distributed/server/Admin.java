package com.distributed.server;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Admin.java
 *
 * Implements an administrator server that can send kill/restart requests to central server.
 *
 * @version 2020-4-21
 */
public class Admin {
    private static String CENTRAL_SERVER_HOST = "127.0.0.1";
    private static int CENTRAL_SERVER_RMI_PORT = 1200;

    public static void main(String[] args) {
        try{
            Registry centralRegistry = LocateRegistry.getRegistry(CENTRAL_SERVER_HOST, CENTRAL_SERVER_RMI_PORT);
            CentralServerInterface centralServer = (CentralServerInterface) centralRegistry.lookup(CentralServer.class.getSimpleName() + CENTRAL_SERVER_RMI_PORT);

            System.out.println("-----------------------------------");
            System.out.println("kill <port>: to kill a server");
            System.out.println("restart <port>: to restart a server");
            System.out.println("-----------------------------------");
            String command = null;
            Scanner input = new Scanner(System.in);
            boolean isAlive = true;
            while (isAlive) {
                System.out.print("admin@127.0.0.1# ");
                String argsLine = input.nextLine();
                String[] arguments = argsLine.split(" ");
                if (argsLine.length() > 0 && arguments.length > 0) {
                    command = arguments[0];
                    try {
                        switch (command) {
                            case "kill":
                                if(arguments.length >= 2) {
                                    try{
                                        int port = Integer.parseInt(arguments[1]);
                                        centralServer.killSlaveServer(port);
                                    } catch(NumberFormatException ex) {
                                        throw new IllegalArgumentException();
                                    }
                                } else throw new IllegalArgumentException();
                                break;
                            case "restart":
                                if(arguments.length >= 2) {
                                    try{
                                        int port = Integer.parseInt(arguments[1]);
                                        centralServer.restartSlaveServer(port);
                                    } catch(NumberFormatException ex) {
                                        throw new IllegalArgumentException();
                                    }
                                } else throw new IllegalArgumentException();
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                        System.err.println("Unsupported arguments. Please try again.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Internal error. Please try again.");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
