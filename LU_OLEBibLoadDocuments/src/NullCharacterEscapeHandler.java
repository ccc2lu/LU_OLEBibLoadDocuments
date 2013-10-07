import java.io.IOException;
import java.io.Writer;
import com.sun.xml.bind.marshaller.*;


public class NullCharacterEscapeHandler implements CharacterEscapeHandler {

    public NullCharacterEscapeHandler() {
        super();
    }


    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer writer) throws IOException {
        writer.write( ch, start, length );
    }
}