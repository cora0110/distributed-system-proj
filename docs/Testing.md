### Testing



start central:

corresponding server files are created

start client \<clientname>:

corresponding client file is created



Client Command:

- help: to show this help message

- register USER PWD: to register a new account with username USER and password PWD

  - Register for the first time -> registered successfully!
  - already exist -> Username already exists
  - if restart server, register info still there -> Username already exists

- login USER PWD: to login using USER and PWD credentials

  - login for the first time -> Logged in successfully as xxx
  - already login -> You're already logged in.
  - wrong password -> Unregistered or password do not match.
  - login a user not register -> Unregistered or password do not match.

- create DOC SEC: to create a new document named DOC and contains SEC sections

  - not login -> You're not logged in
  - 

- edit DOC SEC:(TMP) to edit the section SEC of DOC document (using TMP temporary filename)

- endedit: to stop the current editing session

- showsec DOC SEC:(OUT) to download the content of the SEC section of DOC document (using OUT output filename)

- showdoc DOC:(OUT) to download the content concatenation of all the document's sections (using OUT output filename)

- logout: to logout

  - already login -> Successfully logged out.
  - not login -> You're not logged in

- list: to list all the documents you are able to see and edit

- share USER DOC: to share a document with another user

- news: to get all the news

- receive: to retrieve all the unread chat messages

- send TEXT: to send the TEXT message regarding the document being edited

  