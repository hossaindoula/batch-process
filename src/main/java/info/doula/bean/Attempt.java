package info.doula.bean;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Mohammed Hossain Doula
 *
 * @hossaindoula | @itconquest
 * <p>
 * http://hossaindoula.com
 * <p>
 * https://github.com/hossaindoula
 */
public class Attempt {

    File file;
    private boolean success;
    private boolean systemError;
    private List<String> errors;

    public Attempt(File file) {
        this.file = file;
        this.success = true;
        this.systemError = false;
        this.errors = new LinkedList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean hasSystemError() {
        return systemError;
    }

    public List<String> getErrors() {
        return errors;
    }

    public File getFile() {
        return file;
    }

    public void addError(String error) {
        this.success = false;
        errors.add(error);
    }

    public void addSystemError(String error) {
        addError(error);
        this.systemError = true;
    }

    @Override
    public String toString() {
        return "Attempt[" + file.getName() + ", " + success + "]";
    }
}
