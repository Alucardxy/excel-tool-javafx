package com.alucardxy.exceltool;

import com.alucardxy.exceltool.view.HomeView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication extends AbstractJavaFxApplicationSupport{

	public static void main(String[] args) {
        launch(MainApplication.class,HomeView.class,args);
	}

}
