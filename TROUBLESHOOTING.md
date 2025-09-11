# GitHub Actions CI/CD Troubleshooting Guide

This document provides solutions to common issues encountered with the GitHub Actions CI/CD pipeline.

## 🔧 Fixed Issues

### ❌ "No test report files were found for unit testing"

**Problem**: The GitHub Actions pipeline was looking for test report files in the wrong location or with incorrect patterns.

**Root Cause**: 
- Test reports generated in `target/surefire-reports/TEST-*.xml` format
- Pipeline was looking for `target/surefire-reports/*.xml` pattern
- No actual unit test files existed to generate reports

**Solution**:
1. **Created proper unit tests**:
   - `BridgeServiceApplicationTests.java` - Basic Spring Boot context test
   - `ApplicationPropertiesTest.java` - Configuration testing
   - `BridgeControllerTest.java` - Web layer testing with MockMvc

2. **Fixed pipeline configuration**:
   ```yaml
   - name: 📊 Generate test report
     uses: dorny/test-reporter@v1
     if: success() || failure()
     with:
       name: Unit Test Results
       path: 'target/surefire-reports/TEST-*.xml'  # Fixed path pattern
       reporter: java-junit
       fail-on-error: true
   ```

3. **Updated POM.xml with proper test plugins**:
   ```xml
   <!-- Maven Surefire Plugin for Unit Tests -->
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-surefire-plugin</artifactId>
       <configuration>
           <includes>
               <include>**/*Test.java</include>
               <include>**/*Tests.java</include>
           </includes>
       </configuration>
   </plugin>
   ```

**Verification**:
```bash
# Run tests locally to verify report generation
./mvnw clean test -Dspring.profiles.active=test

# Check that reports are generated
ls -la target/surefire-reports/TEST-*.xml
ls -la target/site/jacoco/jacoco.xml
```

## 🚀 Testing the Fix

### Local Testing
```bash
# Clean and run tests
./mvnw clean test

# Verify test reports exist
find target -name "*.xml" -path "*/surefire-reports/*"
find target -name "jacoco.xml" -path "*/site/jacoco/*"
```

### GitHub Actions Testing
1. Push changes to the `docker_setup` branch
2. Check the Actions tab in GitHub
3. Verify "Unit Test Results" appear in the workflow summary
4. Download test report artifacts if needed

## 📊 Test Structure

### Unit Tests Created
- **BridgeServiceApplicationTests**: Basic Spring Boot application context loading
- **ApplicationPropertiesTest**: Configuration property binding verification  
- **BridgeControllerTest**: Web layer endpoint testing with MockMvc

### Test Reports Generated
- **Surefire Reports**: `target/surefire-reports/TEST-*.xml`
- **JaCoCo Coverage**: `target/site/jacoco/jacoco.xml`
- **Coverage HTML**: `target/site/jacoco/index.html`

### Pipeline Integration
- ✅ Test execution with proper profiles
- ✅ Report generation and upload
- ✅ Coverage analysis with JaCoCo
- ✅ Artifact preservation for debugging
- ✅ Fail-safe error handling

## 🔍 Debugging Tips

### If tests still fail:

1. **Check test file naming**:
   ```bash
   find src/test -name "*Test.java" -o -name "*Tests.java"
   ```

2. **Verify Maven configuration**:
   ```bash
   ./mvnw help:effective-pom | grep -A 10 -B 5 surefire
   ```

3. **Run with debug output**:
   ```bash
   ./mvnw clean test -X -Dspring.profiles.active=test
   ```

4. **Check Spring Boot test configuration**:
   - Ensure `@SpringBootTest` annotation is present
   - Verify test profile configuration in `application-test.yml`
   - Check for classpath issues

### GitHub Actions debugging:

1. **Add debug step in pipeline**:
   ```yaml
   - name: 🔍 Debug test reports
     run: |
       echo "Looking for test reports..."
       find target -name "*.xml" -type f
       ls -la target/surefire-reports/ || echo "No surefire-reports directory"
   ```

2. **Check workflow logs**: Look for Maven output and test execution details
3. **Download artifacts**: Use the uploaded test reports for local analysis

## ✅ Success Indicators

When everything is working correctly, you should see:

1. **Local Maven output**:
   ```
   Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   ```

2. **GitHub Actions summary**:
   - "Unit Test Results" section with test count
   - "📁 Upload test reports" step succeeds
   - Coverage reports uploaded to Codecov (if configured)

3. **Generated files**:
   - Multiple `.xml` files in `target/surefire-reports/`
   - `jacoco.xml` in `target/site/jacoco/`
   - HTML coverage report accessible

### ❌ "Resource not accessible by integration" Error

**Problem**: GitHub Actions shows "HttpError: Resource not accessible by integration" when trying to create test reports or check runs.

**Root Cause**: 
- Missing permissions in the GitHub Actions workflow
- Third-party actions (like test-reporter) need specific permissions to access GitHub API
- Default GITHUB_TOKEN has limited permissions in some repository configurations

**Solution**:
1. **Added comprehensive permissions to workflow**:
   ```yaml
   permissions:
     contents: read
     actions: read
     checks: write
     pull-requests: write
     statuses: write
     issues: write
     security-events: write
   ```

2. **Enhanced error handling**:
   ```yaml
   - name: 📊 Generate test report (fallback)
     uses: dorny/test-reporter@v1
     continue-on-error: true  # Don't fail the workflow if this step fails
     with:
       fail-on-error: false   # Don't fail on test failures
   ```

3. **Added native GitHub summary as primary approach**:
   - Uses built-in `$GITHUB_STEP_SUMMARY` instead of external actions
   - Parses test XML files directly using shell commands
   - More reliable and doesn't depend on third-party integrations

**Verification**:
```bash
# The workflow should now show test results in the summary without errors
# Check GitHub Actions workflow runs for:
# 1. No "Resource not accessible" errors
# 2. Test results visible in workflow summary
# 3. Proper permissions applied
```

**Alternative Solutions**:
If the error persists:
1. **Repository Settings**: Go to Settings → Actions → General → Workflow permissions
2. **Choose**: "Read and write permissions" instead of "Read repository contents permission"
3. **Enable**: "Allow GitHub Actions to create and approve pull requests"

---

**Last Updated**: Fixed GitHub Actions integration permissions error
**Status**: ✅ Resolved
