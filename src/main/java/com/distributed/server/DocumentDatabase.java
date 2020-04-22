package com.distributed.server;

import com.distributed.model.Document;
import com.distributed.model.User;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DocumentDatabase.java
 * Implements data structure and methods for document database.
 *
 * @version 2020-4-21
 */
public class DocumentDatabase implements Serializable {
  private static final long serialVersionUID = 1L;
  private List<Document> documents;

  /**
   * Initializes the internal document's ArrayList.
   */
  public DocumentDatabase() {
    documents = new ArrayList<>();
  }

  /**
   * Creates a new {@code Document} adding it directly to the DocumentsDatabase.
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
   * Checks if the input Document's name already exists or not.
   *
   * @return true if the document already exists, false otherwise
   */
  private boolean alreadyExists(String name) {
    return getDocumentByName(name) != null;
  }

  /**
   * Gets the Document by its name.
   *
   * @param documentName document's name
   * @return the document reference or null if it does not exists yet
   */
  Document getDocumentByName(String documentName) {
    synchronized (documents) {
      for (Document d : documents)
        if (d.getName().compareTo(documentName) == 0) return d;
      return null;
    }
  }

  /**
   * Collects all the documents a given user has access to edit.
   *
   * @param user user reference
   * @return accessible file names
   */
  String[] getAllDocumentsNames(User user) {
    List<String> nameList = new ArrayList<>();
    synchronized (documents) {
      for (Document d : documents)
        if (d.hasPermit(user))
          nameList.add(d.getName());
    }
    return nameList.toArray(new String[0]);
  }

  List<Document> getDocuments() {
    return this.documents;
  }

   void setDocuments(List<Document> documents) {
    this.documents = documents;
  }
}
