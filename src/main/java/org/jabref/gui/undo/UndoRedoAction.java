package org.jabref.gui.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.jabref.gui.DialogService;
import org.jabref.gui.JabRefFrame;
import org.jabref.gui.LibraryTab;
import org.jabref.gui.StateManager;
import org.jabref.gui.actions.SimpleCommand;
import org.jabref.gui.actions.StandardActions;
import org.jabref.logic.l10n.Localization;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ObservableValue;

import com.tobiasdiez.easybind.PreboundBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndoRedoAction extends SimpleCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndoRedoAction.class);

    private final StandardActions action;
    private final JabRefFrame frame;
    private DialogService dialogService;

    private UndoManager undoManager;

    public UndoRedoAction(StandardActions action, JabRefFrame frame, DialogService dialogService,
            StateManager stateManager, UndoManager undoManager) {
        this.action = action;
        this.frame = frame;
        this.dialogService = dialogService;
        this.undoManager = undoManager;

        // ToDo: Rework the UndoManager to something like the following, if it had a
        // property.
        // this.executable.bind(frame.getCurrentBasePanel().getUndoManager().canUndo())
        this.executable.bind(canUndo());
        // this.executable.bind(ActionHelper.needsDatabase(stateManager));
        // new ObservableValue
    }

    public ObservableValue<Boolean> canUndo() {
        return new PreboundBinding<Boolean>() {
            @Override
            protected Boolean computeValue() {
                return undoManager.canUndo();
            }
        };
    }

    public BooleanExpression canUndoOld() {
        return BooleanExpression.booleanExpression(new PreboundBinding<Boolean>() {
            @Override
            protected Boolean computeValue() {
                return undoManager.canUndo();
            }
        });
    }

    @Override
    public void execute() {
        LibraryTab libraryTab = frame.getCurrentLibraryTab();
        if (action == StandardActions.UNDO) {
            try {
                libraryTab.getUndoManager().undo();
                libraryTab.markBaseChanged();
                dialogService.notify(Localization.lang("Undo"));
            } catch (CannotUndoException ex) {
                dialogService.notify(Localization.lang("Nothing to undo") + '.');
            }
            frame.getCurrentLibraryTab().markChangedOrUnChanged();
        } else if (action == StandardActions.REDO) {
            try {
                libraryTab.getUndoManager().redo();
                libraryTab.markBaseChanged();
                dialogService.notify(Localization.lang("Redo"));
            } catch (CannotRedoException ex) {
                dialogService.notify(Localization.lang("Nothing to redo") + '.');
            }

            libraryTab.markChangedOrUnChanged();
        } else {
            LOGGER.debug("No undo/redo action: " + action.name());
        }
    }
}
