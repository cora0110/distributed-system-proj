# distributed-system-final-project

TO Yuting;

ServerImpl.loadUserDB: file name changed from TURING project
        loadDoncumentDB: changed as well
        login: need to add a new method isLoggedIn(String username)

Need to add a readwritelock() in aliveUserDB since we made deletion possible
        


TO Feng Yu:

I'm not sure if you still need/how you use the notification thread, 
please feel free to delete/modify.
    ServerImpl.login(), logout()

Yuting 4/18 1:42, added mutex and logout() in `com.distributed.server.ActiveUserDatabase`.
