Getting Started Accessing Facebook Data
=======================================

Introduction
------------

### What You'll Build

This guide will take you through creating a simple web application that accesses user data from Facebook, including the user's full name and a list of their friends.

### What You'll Need

1. About 15 minutes
2. A favorite text editor or IDE
3. JDK 7 or better
4. Your choice of Maven (3.0+) or Gradle (1.5+)
5. An application ID and secret obtained from [registring an application with Facebook](../gs-register-facebook-app).

### How to Complete this Guide
Like all Spring's [Getting Started guides](/getting-started), you can choose to start from scratch and complete each step, or you can jump past basic setup steps that may already be familiar to you. Either way, you'll end up with working code.

To **start from scratch**, just move on to the next section and start [setting up the project](#scratch).

If you'd like to **skip the basics**, then do the following:

 - [download][zip] and unzip the source repository for this guide—or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-rest-service.git`
 - cd into `gs-rest-service/initial`
 - jump ahead to [creating a representation class](#initial).

And **when you're finished**, you can check your results against the the code in `gs-rest-service/complete`.

<a name="scratch"></a>
Setting up the project
----------------------
First you'll need to set up a basic build script. You can use any build system you like when building apps with Spring, but we've included what you'll need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) here. If you're not familiar with either of these, you can refer to our [Getting Started with Maven](../gs-maven/README.md) or [Getting Started with Gradle](../gs-gradle/README.md) guides.

### Maven

Create a Maven POM that looks like this:

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>org.springframework</groupId>
	<artifactId>gs-accessing-facebook</artifactId>
	<version>1.0</version>

	<parent>
		<groupId>org.springframework.bootstrap</groupId>
		<artifactId>spring-bootstrap-starters</artifactId>
		<version>0.5.0.BUILD-SNAPSHOT</version>
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.bootstrap</groupId>
			<artifactId>spring-bootstrap-web-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.social</groupId>
			<artifactId>spring-social-facebook</artifactId>
			<version>1.1.0.BUILD-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-crypto</artifactId>
			<version>3.1.4.RELEASE</version>
		</dependency>
		<dependency>
			<groupId>org.thymeleaf</groupId>
			<artifactId>thymeleaf-spring3</artifactId>
			<version>2.0.16</version>
		</dependency>		
	</dependencies>

	<repositories>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>http://repo.springsource.org/snapshot</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
		<repository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>http://repo.springsource.org/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
	</repositories>

</project>
```

> > > TODO: mention that we're using Spring Bootstrap's [_starter POMs_](../gs-bootstrap-starter) here.

Experienced Maven users who feel nervous about using an external parent project: don't panic, you can take it out later, it's just there to reduce the amount of code you have to write to get started.

### Gradle

> > > TODO: paste complete build.gradle.

Add the following within the `dependencies { }` section of your build.gradle file:

`build.gradle`
```groovy
compile "org.springframework.bootstrap:spring-bootstrap-web-starter:0.0.1-SNAPSHOT"
compile "org.springframework.social:spring-social-facebook:1.1.0.BUILD-SNAPSHOT"
compile "org.springframework.security:spring-security-crypto:3.1.4.RELEASE"
compile "org.thymeleaf:thymeleaf-spring3:2.0.16"
```

> > > TODO: Adjust dependency versions once the in-memory connection repository stuff is released in a non-snapshot build.

Creating a Configuration Class
------------------------------
The first step is to set up a simple Spring configuration class. It'll look like this:

`src/main/java/hello/HelloFacebookConfiguration.java`

```java
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.springframework.web.servlet.ViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan
public class HelloFacebookConfiguration {

	@Bean
	public TemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}
	
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}
	
	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		return resolver;
	}

}
```

This class is concise, but there's plenty going on under the hood. [`@EnableWebMvc`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/config/annotation/EnableWebMvc.html) handles the registration of a number of components that enable Spring's support for annotation-based controllers—you'll build one of those in an upcoming step. And we've also annotated the configuration class with [`@ComponentScan`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/context/annotation/ComponentScan.html) which tells Spring to scan the `hello` package for those controllers (along with any other annotated component classes).

Also, this example will use [Thymeleaf](../understanding-thymeleaf) as a view implementation to present information to the user. Therefore, the configuration class needs a few essential Thymeleaf beans to enable Thymeleaf templates as Spring views.

<a name="initial"></a>
Enabling Facebook
---------------------------

Since we'll be accessing Facebook data in our application, we'll need to enable Spring Social's Facebook feature by adding the [`@EnableFacebook`](http://static.springsource.org/spring-social-facebook/docs/1.1.x/api/org/springframework/social/facebook/config/annotation/EnableFacebook.html) annotation to the configuration class;

`src/main/java/hello/HelloFacebookConfiguration.java`
```java
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.springframework.web.servlet.ViewResolver;

import org.springframework.social.facebook.config.annotation.EnableFacebook;


@Configuration
@EnableWebMvc
@EnableFacebook(appId="1234567890", appSecret="shhhhh!!!")
@ComponentScan
public class HelloFacebookConfiguration {

	// THYMELEAF CONFIG
	@Bean
	public TemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}
	
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}
	
	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		return resolver;
	}
}
```

Notice that, as shown here, the `appId` and `appSecret` attributes have fake values. For the code to work, you'll need to [obtain a real application ID and secret](../gs-register-facebook-app) and substitute the fake values for the real values given to you by Facebook.


Enabling a Connection Repository
--------------------------------

After a user authorizes your application to access their Facebook data, Spring Social will create a connection. That connection will need to be saved in a connection repository for long-term use.

For the purposes of this guide's sample application, an in-memory connection repository is sufficient. We can enable an in-memory connection repository with the `@EnableInMemoryConnectionRepository` annotation:

`src/main/java/hello/HelloFacebookConfiguration.java`
```java
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.springframework.web.servlet.ViewResolver;

import org.springframework.social.facebook.config.annotation.EnableFacebook;
import org.springframework.social.config.annotation.EnableInMemoryConnectionRepository;

@Configuration
@EnableWebMvc
@EnableFacebook(appId="1234567890", appSecret="shhhhh!!!")
@EnableInMemoryConnectionRepository
@ComponentScan
public class HelloFacebookConfiguration {

	// THYMELEAF CONFIG
	@Bean
	public TemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}
	
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}
	
	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		return resolver;
	}
}
```

> NOTE:
>
> Although an in-memory connection repository is sufficient for testing and small sample applications, you'll want to select a more persistent
> option for real applications. You can use [`@EnableJdbcConnectionRepository`](http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/config/annotation/EnableJdbcConnectionRepository.html) to persist connections to a relational database.


Declaring a User ID Source
--------------------------

Connections represent a 3-way agreement between a user, an application, and an API provider such as Facebook. Although Facebook and the application itself are readily identifiable, you'll need a way to identify the current user. To do that, we'll need to configure an implementation of [`UserIdSource`](http://static.springsource.org/spring-social/docs/1.1.x/api/) in the configuration class:

`src/main/java/hello/HelloFacebookConfiguration.java`
```java
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.springframework.web.servlet.ViewResolver;

import org.springframework.social.facebook.config.annotation.EnableFacebook;
import org.springframework.social.config.annotation.EnableInMemoryConnectionRepository;
import org.springframework.social.UserIdSource;

@Configuration
@EnableWebMvc
@EnableFacebook(appId="1234567890", appSecret="shhhhh!!!")
@EnableInMemoryConnectionRepository
@ComponentScan
public class HelloFacebookConfiguration {
	@Bean
	public UserIdSource userIdSource() {
		return new UserIdSource() {			
			@Override
			public String getUserId() {
				return "testuser";
			}
		};
	}

	// THYMELEAF CONFIG
	@Bean
	public TemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}
	
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}
	
	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		return resolver;
	}

}
```

Here, the `userIdSource` bean is defined by an inner-class that always returns "testuser" as the user ID. Thus there is only one user of our sample application.

In a real application, you'll probably want to create an implementation of `UserIdSource` that determines the user ID from the currently authenticated user (perhaps by consulting with an [`Authentication`](http://static.springsource.org/spring-security/site/docs/3.2.x/apidocs/org/springframework/security/core/Authentication.html) obtained from Spring Security's [`SecurityContext`](http://static.springsource.org/spring-security/site/docs/3.2.x/apidocs/org/springframework/security/core/context/SecurityContext.html).)



Declaring a Connection Controller
---------------------------------

Obtaining user authorization from Facebook involves a "dance" of redirects between the application and Facebook. This "dance" is formally known as [OAuth 2](../understanding-oauth)'s _Authorization Code Grant_.

Don't worry if you don't know much about OAuth. Spring Social's [`ConnectController`](http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/connect/web/ConnectController.html) will take care of the OAuth dance for you. Just declare `ConnectController` as a bean like this:

`src/main/java/hello/HelloFacebookConfiguration.java`
```java
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.springframework.web.servlet.ViewResolver;

import org.springframework.social.facebook.config.annotation.EnableFacebook;
import org.springframework.social.config.annotation.EnableInMemoryConnectionRepository;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.web.ConnectController;

@Configuration
@EnableWebMvc
@EnableFacebook(appId="1234567890", appSecret="shhhhh!!!")
@EnableInMemoryConnectionRepository
@ComponentScan
public class HelloFacebookConfiguration {
	@Bean
	public UserIdSource userIdSource() {
		return new UserIdSource() {			
			@Override
			public String getUserId() {
				return "testuser";
			}
		};
	}

	@Bean
	public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator, ConnectionRepository connectionRepository) {
		return new ConnectController(connectionFactoryLocator, connectionRepository);
	}

	// THYMELEAF CONFIG
	@Bean
	public TemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}
	
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}
	
	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		return resolver;
	}

}
```

Notice that `ConnectController` is created by injecting a [`ConnectionFactoryLocator`](http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/connect/ConnectionFactoryLocator.html) and a [`ConnectionRepository`](http://static.springsource.org/spring-social/docs/1.1.x/api/org/springframework/social/connect/ConnectionRepository.html) via the constructor. We won't need to explicitly declare these beans, however. The `@EnableFacebook` annotation will make sure that a `ConnectionFactoryLocator` bean is created and the `@EnableInMemoryConnectionRepository` annotation will create an in-memory implementation of `ConnectionRepository`.

Creating the Connection Status Views
------------------------------------
Although much of what `ConnectController` does involves redirecting to Facebook and handling a redirect from Facebook, it also shows connection status when a GET request to /connect is made. It will defer to a view whose name is connect/{provider ID}Connect when no existing connection is available and to connect/{providerId}Connected when a connection exists for the provider. In our case, {provider ID} is "Facebook".

`ConnectController` does not define its own connection views, so we'll need to create them ourselves. First, here's a Thymeleaf view to be shown when no connection to Facebook exists:

`src/main/webapp/connect/facebookConnect.html`
```html
<html>
	<head>
		<title>Hello Facebook</title>
	</head>
	<body>
		<h3>Connect to Facebook</h3>
		
		<form action="/connect/facebook" method="POST">
			<div class="formInfo">
				<p>You aren't connected to Facebook yet. Click the button to connect Spring Social Showcase with your Facebook account.</p>
			</div>
			<p><button type="submit">Connect to Facebook</button></p>
		</form>
	</body>
</html>
```

The form on this view will POST to /connect/Facebook, which will kick off the OAuth 2 authorization code flow.

Here's the view to be displayed when a connection exists:

`src/main/webapp/connect/facebookConnected.html`
```html
<html>
	<head>
		<title>Hello Facebook</title>
	</head>
	<body>
		<h3>Connected to Facebook</h3>
		
		<p>
			You are now connected to your Facebook account.
			Click <a href="/">here</a> to see your Facebook friends.
		</p>		
	</body>
</html>
```

Fetching User Data from Facebook
--------------------------------
All that's left is to create a Spring MVC controller to handle requests for the home page:

`src/main/java/hello/HelloFacebookController.java`
```java
package hello;

import javax.inject.Inject;

import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HelloFacebookController {
	
	private Facebook facebook;

	@Inject
	public HelloFacebookController(Facebook facebook) {
		this.facebook = facebook;		
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public String helloFacebook(Model model) {
		if (!facebook.isAuthorized()) {
			return "redirect:/connect/facebook";
		}
	
		model.addAttribute(facebook.userOperations().getUserProfile());
		PagedList<FacebookProfile> friends = facebook.friendOperations().getFriendProfiles();
		model.addAttribute("friends", friends);
		return "hello";
	}
	
}
```

The `helloFacebook()` method is annotated with `@RequestMapping` to indicate that it should handle GET requests for the root path (/). The first thing it does is check to see if the user has authorized the application to access their Facebook data. If not, then the user is redirected to `ConnectController` where they may kick off the authorization process.

If the user has authorized the application to access their Facebook data, then it will fetch the user's profile as well as a list of profiles belonging to the user's friends. Both are placed into the model to be displayed by the view identified as "hello".

Displaying User Data
--------------------

Speaking of the "hello" view, here it is as a Thymeleaf template:

`src/main/webapp/hello.html`
```html
<html>
	<head>
		<title>Hello Facebook</title>
	</head>
	<body>
		<h3>Hello, <span th:text="${facebookProfile.name}">Some User</span>!</h3>
		
		<h4>These are your friends:</h4>
		
		<ul>
			<li th:each="friend:${friends}" th:text="${friend.name}">Friend</li>
		</ul>
	</body>
</html>
```

Building an executable JAR
--------------------------

Add the following to your `pom.xml` file (keeping any existing properties or plugins intact):

`pom.xml`
```xml
<properties> 
    <start-class>hello.HelloWorldConfiguration</start-class>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

The following will produce a single executable JAR file containing all necessary dependency classes:
```
$ mvn package
```

Running the Service
-------------------------------------

Now you can run it from the jar as well, and distribute that as an executable artifact:
```
$ java -jar target/gs-accessing-facebook-1.0.jar

... app starts up ...
```

Once the application starts up, you can point your web browser to http://localhost:8080. Since no connection has been established yet, you should see this screen prompting you to connect with Facebook:

![No connection to Facebook exists yet.](images/connect.png)

When you click the "Connect to Facebook" button, the browser will be redircted to Facebook for authorization:

![Facebook needs your permission to allow the application to access your data.](images/fbauth.png)

At this point, Facebook is asking if you'd like to allow the sample application to access your public profile and list of friends. Click "Okay" to grant permission.

Once permission has been granted, Facebook will redirect the browser back to the application and a connection will be created and stored in the connection repository. You should see this page indicating that a connection was successful:

![A connection with Facebook has been created.](images/connected.png)

If you click on the link on the connection status page, you will be taken to the home page. This time, now that a connection has been created, you'll be shown your name on Facebook as well as a list of your friends:

![Guess noone told you life was gonna be this way.](images/friends.png)


Congratulations! You have just developed a simple web application that uses Spring Social to connect a user with Facebook and to retrieve some data from the user's Facebook profile.

