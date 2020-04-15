package Mapping;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import keystrokesimulator.KeystrokeSimulator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class Mapping {
   public Map<String, Note> notes = new HashMap();
   public Map<String, Chord> chords = new HashMap();
   public Map<String, NGram> ngrams = new HashMap();
   public Map<String, NGram> words = new HashMap();
   public Map<String, Letter> letters = new HashMap();
   Document xmlMapping;
   private static Vector noteNames;

   public Mapping(String mappingPath) throws JDOMException, IOException {
      SAXBuilder builder = new SAXBuilder();
      URL mappingURL = KeystrokeSimulator.class.getResource(mappingPath);
      this.xmlMapping = builder.build(mappingURL);
      this.initMapping();
   }

   private void initMapping() {
      String[] names = new String[]{"C", "CIS", "D", "DIS", "E", "F", "FIS", "G", "GIS", "A", "AIS", "B"};
      noteNames = new Vector(Arrays.asList(names));
      Element mapping = this.xmlMapping.getRootElement();
      List<Element> letterElements = mapping.getChildren("letter");
      List<Element> wordElements = mapping.getChildren("word");
      List<Element> ngramElements = mapping.getChildren("ngram");

      int i;
      Element ng;
      int position;
      for(i = 0; i < letterElements.size(); ++i) {
         ng = (Element)letterElements.get(i);
         Letter letter = new Letter(ng.getAttributeValue("name"));
         List<Element> nElements = ng.getChildren("note");
         ArrayList<Note> ns = new ArrayList();

         for(int j = 0; j < nElements.size(); ++j) {
            Element nE = (Element)nElements.get(j);
            String name = nE.getAttributeValue("name").toUpperCase();
            position = Integer.parseInt(nE.getAttributeValue("octave"));
            Note n = new Note(name, position, letter, getNoteValue(name, position));
            ns.add(n);
            this.notes.put(n.name + n.octave, n);
         }

         letter.mapTo(ns);
         this.letters.put(letter.name, letter);
      }

      NGram ngram;
      Element cElement;
      List chordNotes;
      String[] ns;
      int j;
      Chord chord;
      Element cE;
      for(i = 0; i < ngramElements.size(); ++i) {
         ng = (Element)ngramElements.get(i);
         ngram = new NGram(ng.getAttribute("name").getValue());
         cElement = ng.getChild("chord");
         chordNotes = cElement.getChildren("cnote");
         ns = new String[chordNotes.size()];

         for(j = 0; j < chordNotes.size(); ++j) {
            cE = (Element)chordNotes.get(j);
            position = Integer.parseInt(cE.getAttributeValue("position"));
            ns[position - 1] = cE.getAttributeValue("name").toUpperCase();
         }

         chord = new Chord(ns, ngram);
         this.chords.put(chord.toString(), chord);
         ngram.mapTo(chord);
         this.ngrams.put(ngram.name, ngram);
      }

      for(i = 0; i < wordElements.size(); ++i) {
         ng = (Element)wordElements.get(i);
         ngram = new NGram(ng.getAttribute("name").getValue());
         cElement = ng.getChild("chord");
         chordNotes = cElement.getChildren("cnote");
         ns = new String[chordNotes.size()];

         for(j = 0; j < chordNotes.size(); ++j) {
            cE = (Element)chordNotes.get(j);
            position = Integer.parseInt(cE.getAttributeValue("position"));
            ns[position - 1] = cE.getAttributeValue("name").toUpperCase();
         }

         chord = new Chord(ns, ngram);
         this.chords.put(chord.toString(), chord);
         ngram.mapTo(chord);
         this.words.put(ngram.name, ngram);
      }

   }

   public static int getNoteValue(String keyName, int keyOctave) {
      if (keyName.equals("SPACE") || keyName.equals("BACKSPACE") || keyName.equals("ENTER")) {
         return -1;
      } else {
         int index = noteNames.indexOf(keyName);
         int o = (keyOctave + 1) * 12;
         return o + index;
      }
   }
}
