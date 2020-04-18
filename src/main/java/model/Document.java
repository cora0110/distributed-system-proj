package model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Document implements Serializable {
  private static final long serialVersionUID = -5250135537941945156L;

  private String name;
  private User creator;
  private List<User> authors;
  private List<Section> sections;

  public Document(String name, User creator, List<Section> sections) {
    this.name = name;
    this.creator = creator;
    this.sections = sections;
  }

  /**
   * Creates a new document with path.
   */
  public static Document create(User creator, String directory, int sectionsNumber, String name) throws IOException {
    String path = directory + "/" + name;
    File document = new File(path);
    if (!document.exists() || !document.isDirectory()) {
      boolean mkdir = document.mkdir();
      if (!mkdir) throw new RuntimeException("Unable to create document.");
    }
    List<Section> sections = new ArrayList<>();
    long timestamp = System.currentTimeMillis();
    for (int i = 0; i < sectionsNumber; i++) {
      Section sec = new Section(path, String.valueOf(timestamp + i));
      sections.add(sec);
      File sectionFile = new File(sec.getPath());
      sectionFile.createNewFile();
    }
    return new Document(name, creator, sections);
  }

  public Section getSectionByIndex(int index) {
    return sections.get(index);
  }

  public void addAuthor(User user) {
    authors.add(user);
  }

  public boolean hasPermit(User user) {
    return creator.equals(user) || authors.contains(user);
  }

  /**
   * Gets all the sections currently being edited.
   */
  public List<Section> getOccupiedSections() {
    List<Section> editing = new ArrayList<>();
    for (int i = 0; i < sections.size(); i++) {
      Section section = sections.get(i);
      if (section.getOccupant() != null) {
        editing.add(section);
      }
    }
    return editing;
  }


}
