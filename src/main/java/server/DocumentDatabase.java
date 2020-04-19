package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Document;
import model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDatabase {
  private static final long serialVersionUID = 1L;
  private List<Document> documents;

  /**
   * Initializes the internal document's {@code ArrayList}.
   */
  public DocumentDatabase() {
    documents = new ArrayList<>();
  }

  /**
   * Creates a new {@code Document} adding it directly to the {@code DocumentsDatabase}.
   *
   * @param path           new document file path
   * @param sectionsNumber new document sections number
   * @param name           new document's name
   * @param creator        new document's owner
   * @throws IOException if an I/O error occurs
   */
  public void createNewDocument(String path, int sectionsNumber, String name, User creator) {
    try {
      Document document;
      if (alreadyExists(name)) throw new IOException("Document already exists");
      else document = Document.create(creator, path, sectionsNumber, name);
      synchronized (documents) {
        documents.add(document);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks if the input {@code Document}'s name already exists or not.
   *
   * @return true if the document already exists, false otherwise
   */
  private boolean alreadyExists(String name) {
    return getDocumentByName(name) != null;
  }

  /**
   * Looks for the {@code Document} based on its name.
   *
   * @param documentName document's name
   * @return the document reference or null if it does not exists yet
   */
  public Document getDocumentByName(String documentName) {
    synchronized (documents) {
      for (Document d : documents)
        if (d.getName().compareTo(documentName) == 0) return d;
      return null;
    }
  }

  /**
   * Collects all the documents a give {@code User} can access to.
   *
   * @param user user reference
   * @return accessible file names
   */
  public String[] getAllDocumentsNames(User user) {
    List<String> nameList = new ArrayList<>();
    synchronized (documents) {
      for (Document d : documents)
        if (d.hasPermit(user))
          nameList.add(d.getName());
    }
    return nameList.toArray(new String[0]);
  }
}
