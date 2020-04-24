## distributed-system-final-project

### Getting Started
Start Central Server and affiliated servers:

(default central port: 1200, default server ports: 1300, 1400, 1500, 1600, 1700):
> java -jar CentralServer.jar

Start Client:

(Please specify the name of current client, using as the directory name to store files)
> java -jar Client.jar ${CLIENT_NAME}

Start Admin:

> java -jar Admin.jar

### Testing


After starting central server: corresponding server directories are created

After starting client \<clientname>: corresponding client directory is created



Client Command:

- help: to show help message

- register USER PWD: to register a new account with username USER and password PWD

  - register for the first time -> registered successfully!
  - already exist -> Username already exists
  - if restart server, register info still there -> Username already exists

- login USER PWD: to login using USER and PWD credentials

  - login for the first time -> Logged in successfully
  - already login -> You're already logged in.
  - wrong password -> Unregistered or password do not match.
  - login a user that did not register -> Unregistered or password do not match.

- create DOC SEC: to create a new document named DOC and contains SEC sections

  - not login -> You're not logged in
  - create a new doc -> Successfully create a new document (all the server directories can see the new doc)
  - doc already exist -> Document already exists.

- edit DOC SEC (TMP): to edit the section SEC of DOC document (using TMP temporary filename)

    - edit a existing section: corresponding section file is downloaded to the client directory
    - edit a non-existing doc or section: Document does not exist./ Section does not exist.
    - edit a section someone else is editing: The section is being edited

- endedit: to stop the current editing session

    - endedit a section: successfully upload the section file to all the replica servers
    - not editing a section: You're not editing any section

- showsec DOC SEC (OUT): to download the content of the SEC section of DOC document (using OUT output filename)
    
    - if succeed -> corresponding section file is downloaded to the client directory
    
  - if not exist -> Section does not exist.
  - someone is editing the section -> xx is editing the section right now
  
- showdoc DOC (OUT): to download the content concatenation of all the document's sections (using OUT output filename)
    
- if succeed -> corresponding doc file is downloaded to the client directory
    
    - if not exist -> Document does not exist.
    - some sections are being edited -> These are the on editing sections: [sectionNum]
    - no section being edited -> No one is editing this document
    
- logout: to logout

    - already logged in -> Successfully logged out.
    - not login -> You're not logged in
    - haven't end edit -> You should 'endedit' before logging out

- list: to list all the documents you are able to see and edit

  - if none -> none
  - if exist -> list them \<file1>,\<file2>...

- share USER DOC: to share a document with another user

  - non-existing doc -> Document does not exist.
  - inaccessible doc -> You do not have access.
  - non-existing target user -> The target user does not exist.
  - success -> Document shared successfully

- news: to get all the news

  - if any -> display the news
  - if none -> No news available

- receive: to retrieve all the unread chat messages
  - is editing doc -> [sender] - message content
  - not editing any doc -> You're not editing any document

- send TEXT: to send the TEXT message regarding the document being edited
  
  - not editing doc -> You're not editing any document
  
    


Consistency Test

- 5 replicas, register in client1, then:
  - register in client2 -> Username already exists
  
- login in client2 -> Logged in successfully
  
    

Fault Tolerance Test

We have five replicas:

- Kill one server -> commit
- kill two servers -> commit
- kill >= three serverss -> abort



Recover Data Test

- First kill one server and then client1 creates a new document -> commit (The dead server doesn't have this new doc)
- Central server gets notification that this server is dead
- Restart the dead server -> The dead server recovers data from a helper server and now it contains the new doc

