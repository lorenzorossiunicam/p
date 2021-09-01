package it.unicam.pros.purple.gui.ui.components;

import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.io.InputStream;

public class Uploader {

    private InputStream stream;
    private Upload upload;

    public Uploader(String mimeType, String... extensions){
        MemoryBuffer buffer = new MemoryBuffer();
        this.upload = new Upload(buffer);
        this.upload.setAcceptedFileTypes(extensions);
        upload.addSucceededListener(event -> {
            stream = createStream(event.getMIMEType(),  buffer.getInputStream(), mimeType);
        });
    }

    public Upload getUploadComponent(){
        return upload;
    }

    public InputStream getStream(){
        return stream;
    }

    private InputStream createStream(String mimeType, InputStream stream,String mime) {
        if (mimeType.startsWith(mime)) {
            return stream;
        }else{
            throw new IllegalArgumentException("File corrupted.");
        }
    }
}
