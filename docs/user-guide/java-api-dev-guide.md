# User Guide - Using the Java APIs

(I'm slapping text in to try to capture ideas, but doing minimal editing)

We release a `cohort-engine` jar containing public APIs meant to provide developers a way to write applications that
interface with the CQL Engine's underlying logic. This page contains notes on getting your development environment
set up to code against our Java APIs.

(Maybe have a note recommending that a user should read the getting started user guide to understand various pre-reqs?)

## Available Jars
We publish our jars to GitHub packages. Both [release jars](https://github.com/Alvearie/quality-measure-and-cohort-service/releases)
and [snapshot builds](https://github.com/Alvearie/quality-measure-and-cohort-service/packages/471313?version=0.0.1-SNAPSHOT)
are available. If developing against the snapshot builds, keep in mind that APIs and functionality may be unstable.
We highly recommend devloping against the release jars when possible.

### Setting up GitHub Packages
GitHub packages requires authentication whe downloading published dependencies. As such, there is additional setup that
needs to be performed before you will be able to download jars for use in your project. These instructions use maven
for configuring and building a project using our Java APIs.

(copied and only slightly modified from dev-guide/getting-started.md. Maybe this can be de-duplicated)

To access dependencies on GitHub packages, you must first create a Personal Access Token by going to https://github.com/settings/tokens and logging in with your github userid. Click "Personal access tokens" in the left side menu, then click the "Generate new token" button. Under the "Select Scopes," check read:packages and click the "Generate Token" button. Copy the generated token string as it will be used in the settings.xml updates below.

Update your maven ~/.m2/settings.xml to allow your local maven to retrieve the dependencies for the this project. Below is an example settings.xml file:


	<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
		<servers>
			<server>
				<id>github</id>
				<username>REPLACE_WITH_YOUR_GITHUB_ID</username>
				<!-- generate at https://github.com/settings/tokens using read:packages -->
				<password>REPLACE_WITH_YOUR_PERSONAL_ACCESS_TOKEN</password>
			</server>
		</servers>
		
		<profiles>
			<profile>
				<id>github</id>
				<repositories>
					<repository>
						<id>central</id>
						<url>https://repo1.maven.org/maven2</url>
						<releases><enabled>true</enabled></releases>
						<snapshots><enabled>true</enabled></snapshots>
					</repository>
					<repository>
						<id>github</id>
						<name>GitHub Alvearie Apache Maven Packages</name>
						<url>https://maven.pkg.github.com/Alvearie/quality-measure-and-cohort-service</url>
					</repository>
				</repositories>
			</profile>
		</profiles>
	
		<activeProfiles>
			<activeProfile>github</activeProfile>
		</activeProfiles>
	</settings>
	
(end copy)

Once maven is configured, you should be able to add the `cohort-engine` dependency to your project to have access to
our CQL evaluation entrypoints. For example, this adds the `1.0.0` release to your project.

```xml
<dependency>
  <groupId>com.ibm.cohort</groupId>
  <artifactId>cohort-engine</artifactId>
  <version>1.0.0</version>
</dependency>
```

[Other published dependencies](https://github.com/orgs/Alvearie/packages?repo_name=quality-measure-and-cohort-service)
can similarly be added to your project.

#### Troubleshooting
If attempting to build your project results in a failure with an `Authorization failed` or a `403` error code, a likely
culprit is the github token mentioned during setup. The recommended solution is to make a new token with
the correct permissions and reconfigure your settings.xml file. Then, attempt to rebuild the project.

## Entrypoints for Java Evaluation
We have two primary Java classes meant to be used during CQL evaluation development: the [CqlEvaluator](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-engine/src/main/java/com/ibm/cohort/engine/CqlEvaluator.java)
and [MeasureEvaluator](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-engine/src/main/java/com/ibm/cohort/engine/measure/MeasureEvaluator.java).

Example usage for these classes are available in the `CohortCLI` and `MeasureCLI` (which are each described on
the [getting started](user-guide/getting-started.md) page).

### CohortEvaluator vs. MeasureEvaluator 
(Not sure if a side-by-side would be useful or not. If it is, I'm not sure how much detail to get into)

## Any other setup (or just reference getting started)?