package hello;

import javax.inject.Inject;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.google.api.Google;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HelloController {

    private Facebook facebook;
    private Google google;
	private ConnectionRepository connectionRepository;

    @Inject
    public HelloController(Facebook facebook, Google google, ConnectionRepository connectionRepository) {
        this.facebook = facebook;
        this.google = google;
		this.connectionRepository = connectionRepository;
    }
/*
    @RequestMapping(method=RequestMethod.GET)
    public String helloFacebook(Model model) {
        if (connectionRepository.findPrimaryConnection(Facebook.class) == null) {
            return "redirect:/connect/facebook";
        }

        model.addAttribute("facebookProfile", facebook.userOperations().getUserProfile());
        PagedList<Post> feed = facebook.feedOperations().getFeed();
        model.addAttribute("feed", feed);
        return "hello";
    }
*/
    @RequestMapping(method=RequestMethod.GET)
    public String helloGoogle(Model model) {
        if (connectionRepository.findPrimaryConnection(Google.class) == null) {
            return "redirect:/connect/google";
        }

        model.addAttribute("googleProfile", google.plusOperations().getGoogleProfile());
//        PagedList<Post> feed = google.plusOperations()...();
//        model.addAttribute("feed", feed);
        return "hello";
    }

}
