package ch.uzh.ifi.hase.soprafs22.service;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class ImageService {
    private int index = -1;
    private String[] picturePaths = new String[] { "src/main/java/ch/uzh/ifi/hase/soprafs22/Images/cute-watercolor-calf-baby-bull-illustration-cattle-farm-animal-cute-watercolor-calf-baby-bull-illustration-cattle-farm-animal-new-197022137.jpeg",
            "src/main/java/ch/uzh/ifi/hase/soprafs22/Images/funny-animal-super-hero-squirrel-unbuckle-his-fur-isolated-white-62400580.jpeg",
            "src/main/java/ch/uzh/ifi/hase/soprafs22/Images/lemur-animal-reading-book-glasses-woods-education-school-concept-51901433.jpeg",
            "src/main/java/ch/uzh/ifi/hase/soprafs22/Images/raccoon-abstract-animal-portrait-watercolour-illustration-white-background-abstract-animal-portrait-raccoon-watercolour-177712323.jpeg"
    };

    public BufferedImage JavaImageIOTest()
    {
        if(index ==3){
            index = 0;
        }
        try
        {
            // the line that reads the image file
            index +=1;
            BufferedImage image = ImageIO.read(new File(picturePaths[index]));
            return image;


            // work with the image here ...
        }
        catch (IOException e)
        {
            // log the exception
            // re-throw if desired
        }
        return null;
    }

}