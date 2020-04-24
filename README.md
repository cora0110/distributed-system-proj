## distributed-system-final-project

###Getting Started
Start Central Server and affiliated servers:

(default central port: 1200, default server ports: 1300, 1400, 1500, 1600, 1700):
> java -jar CentralServer.jar ${CENTRAL_PORT} ${PORT1} ${PORT2} ${PORT3} ${PORT4} ${PORT5}

Start Client:

(Please specify the name of current client, using as the directory name to store files)
> java -jar Client.jar ${CLIENT_NAME} ${PORT} ${TIMEOUT_MILLISECONDS}

### Testing


After starting central: corresponding server files are created

After starting client \<clientname>: corresponding client file is created



Client Command:

- help: to show help message

- register USER PWD: to register a new account with username USER and password PWD

  - Register for the first time -> registered successfully!
  - already exist -> Username already exists
  - if restart com.distributed.server, register info still there -> Username already exists

- login USER PWD: to login using USER and PWD credentials

  - login for the first time -> Logged in successfully as xxx
  - already login -> You're already logged in.
  - wrong password -> Unregistered or password do not match.
  - login a user not register -> Unregistered or password do not match.

- create DOC SEC: to create a new document named DOC and contains SEC sections

  - not login -> You're not logged in
  - login and doc not already exist -> Successfully create a new document (all the com.distributed.server directories can see the new doc)
  - doc already exist -> Document already exists.

- edit DOC SEC:(TMP) to edit the section SEC of DOC document (using TMP temporary filename)

- endedit: to stop the current editing session

- showsec DOC SEC:(OUT) to download the content of the SEC section of DOC document (using OUT output filename)
    - if not exist -> Section does not exist.
    - some sections are being edited -> These are the on editing sections: [sectionNum]
    - no section being edited -> No one is editing this document
  
- showdoc DOC:(OUT) to download the content concatenation of all the document's sections (using OUT output filename)
    - if not exist -> Document does not exist.

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

- receive: to retrieve all the unread com.distributed.chat messages
  - is editing doc -> [sender] - message content
  - not editing any doc -> You're not editing any document

- send TEXT: to send the TEXT message regarding the document being edited
  - not editing doc -> You're not editing any document


Consistency test

- 5 replicas, register in client1, then:
  - register in client2 -> Username already exists
  - login in client2 -> Logged in successfully

- Kill one server -> commit
- kill two servers -> commit
- kill >= three serverss -> abort

