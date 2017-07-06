package com.github.monster860.fastdmm;

import javax.swing.*;
import java.io.FileNotFoundException;

public class AutosaveDaemon implements Runnable
{
    private FastDMM editor;

    public AutosaveDaemon(FastDMM editor)
    {
        this.editor = editor;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(60000);
                editor.loadedMaps.forEach((dmm) ->
                {
                    try
                    {
                        dmm.save();
                    }
                    catch(FileNotFoundException e)
                    {
                        JOptionPane.showMessageDialog(editor, "Tried to save map " + dmm.file.getName() + " but got FileNotFoundException", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
            catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
