package org.digitalcampus.oppia.utils.resources;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Joseba on 28/01/2015.
 */
public class ExternalResourceOpener{

	public static Intent getIntentToOpenResource(Context ctx, Uri resourceUri, String resourceMimeType){

        // check there is actually an app installed to open this filetype
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(resourceUri, resourceMimeType);
        if(resourceUri.getPath().contains("illustration") || resourceUri.getPath().contains("lsm")) {
        	intent = pdfViewer(ctx, resourceUri);
        }
        
        PackageManager pm = ctx.getPackageManager();

        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        boolean appFound = false;
        for (ResolveInfo info : infos) {
            IntentFilter filter = info.filter;
            if (filter != null && filter.hasAction(Intent.ACTION_VIEW)) {
                // Found an app with the right intent/filter
                appFound = true;
            }
        }

        //In case there is a valid filter, we return the intent, otherwise null
        return (appFound? intent : null);



    }
    
    
	public static Intent pdfViewer(Context ctx, Uri resourceUri) {
		File fileBrochure;
		String fileName;
	    if(resourceUri.getPath().contains("illustration")) {
	    	fileName="IPCToolkit.pdf";
	    }
	    else {
	    	fileName=resourceUri.getPath().substring(resourceUri.getPath().lastIndexOf('/'));
	    	if(fileName.equalsIgnoreCase("/anc.png") || fileName.equalsIgnoreCase("/Antenatal Care") )
	    		fileName = "anc.pdf";
	    	if(fileName.equalsIgnoreCase("/preparedness.png") || fileName.equalsIgnoreCase("/Birth Preparedness") )
	    		fileName = "preparedness.pdf";
	    	if(fileName.equalsIgnoreCase("/spacing.png") || fileName.equalsIgnoreCase("/Birth Spacing") )
	    		fileName = "spacing.pdf";
	    	if(fileName.equalsIgnoreCase("/child_health.png") || fileName.equalsIgnoreCase("/Child Health") )
	    		fileName = "child_health.pdf";
	    	if(fileName.equalsIgnoreCase("/early_newborn.png") || fileName.equalsIgnoreCase("/Early Newborn") )
	    		fileName = "early_newborn.pdf";
	    	if(fileName.equalsIgnoreCase("/pnc.png") || fileName.equalsIgnoreCase("/Postnatal Care") )
	    		fileName = "pnc.pdf";
	    }

	    fileBrochure = new File(Environment.getExternalStorageDirectory().getPath() + "/"+fileName);

	    if (!fileBrochure.exists())
	    {
	        CopyAssetsbrochure(ctx, fileName);
	    } 
	
	   /** PDF reader code */
	   File file = new File(Environment.getExternalStorageDirectory().getPath() + "/"+fileName);        
	
	   Intent intent = new Intent(Intent.ACTION_VIEW);
	   intent.setDataAndType(Uri.fromFile(file),"application/pdf");
	   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   try 
	   {
	       return intent;
	   } 
	   catch (ActivityNotFoundException e) 
	   {
	        return null;
	   }
	}

	//method to write the PDFs file to sd card
    private static void CopyAssetsbrochure(Context ctx, String filename) {
       AssetManager assetManager = ctx.getAssets();
       String[] files = null;
       try 
       {
           files = assetManager.list("");
       } 
       catch (IOException e)
       {
           Log.d("tag", e.getMessage());
       }
       for(int i=0; i<files.length; i++)
       {
           String fStr = files[i];
           if(fStr.equalsIgnoreCase(filename))
           {
               InputStream in = null;
               OutputStream out = null;
               try 
               {
                 in = assetManager.open(files[i]);
                 out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + files[i]);
                 copyFile(in, out);
                 in.close();
                 in = null;
                 out.flush();
                 out.close();
                 out = null;
                 break;
               } 
               catch(Exception e)
               {
                   Log.e("tag", e.getMessage());
               } 
           }
       }
   }
   
   private static void copyFile(InputStream in, OutputStream out) throws IOException {
       byte[] buffer = new byte[1024];
       int read;
       while((read = in.read(buffer)) != -1){
         out.write(buffer, 0, read);
       }
   }
    
}
