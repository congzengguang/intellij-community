package com.intellij.ide.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ScrollPaneFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author nik
 */
public abstract class ChooseElementsDialog<T> extends DialogWrapper {
  protected ElementsChooser<T> myChooser;
  private String myDescription;

  public ChooseElementsDialog(Project project, List<? extends T> items, String title, final String description) {
    this(project, items, title, description, false);
  }

  public ChooseElementsDialog(Project project, List<? extends T> items, String title, final String description, boolean sort) {
    super(project, true);
    myDescription = description;
    initializeDialog(items, title, sort);
  }

  public ChooseElementsDialog(Component parent, List<T> items, String title) {
    this(parent, items, title, null, false);
  }

  public ChooseElementsDialog(Component parent, List<T> items, String title, @Nullable String description, final boolean sort) {
    super(parent, true);
    myDescription = description;
    initializeDialog(items, title, sort);
  }

  private void initializeDialog(final List<? extends T> items, final String title, boolean sort) {
    setTitle(title);
    myChooser = new ElementsChooser<T>(false) {
      protected String getItemText(@NotNull final T item) {
        return ChooseElementsDialog.this.getItemText(item);
      }
    };
    myChooser.setColorUnmarkedElements(false);

    List<? extends T> elements = new ArrayList<T>(items);
    if (sort) {
      Collections.sort(elements, new Comparator<T>() {
        public int compare(final T o1, final T o2) {
          return getItemText(o1).compareToIgnoreCase(getItemText(o2));
        }
      });
    }
    setElements(elements, elements.size() > 0 ? elements.subList(0, 1) : Collections.<T>emptyList());
    myChooser.getComponent().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doOKAction();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
    myChooser.getComponent().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isPopupTrigger() && !e.isConsumed()) {
          e.consume();
          doOKAction();
        }
      }
    });
    init();
  }

  protected abstract String getItemText(T item);

  public List<T> getChosenElements() {
    return myChooser.getSelectedElements();
  }

  public JComponent getPreferredFocusedComponent() {
    return myChooser.getComponent();
  }

  protected JComponent createCenterPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(ScrollPaneFactory.createScrollPane(myChooser.getComponent()), BorderLayout.CENTER);
    if (myDescription != null) {
      panel.add(new JLabel(myDescription), BorderLayout.NORTH);
    }
    return panel;
  }

  private void setElements(final Collection<? extends T> elements, final Collection<? extends T> elementsToSelect) {
    myChooser.clear();
    for (final T item : elements) {
      myChooser.addElement(item, false, createElementProperties(item));
    }
    myChooser.selectElements(elementsToSelect);
  }

  private ElementsChooser.ElementProperties createElementProperties(final T item) {
    return new ElementsChooser.ElementProperties() {
      public Icon getIcon() {
        return getItemIcon(item);
      }

      public Color getColor() {
        return null;
      }
    };
  }

  @Nullable 
  protected abstract Icon getItemIcon(T item);
}
