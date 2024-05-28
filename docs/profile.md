# Profile

## Components

### `MimiProfileFragment`

The Mimi Profile Fragment (`io.mimi.sdk.profile.MimiProfileFragment`) is the MSDK UI entry-point which provides features for users to onboard and personalize their sound. In addition, login and signup options are also provided, allowing users to load their previously created Mimi data or save their current data with a Mimi account.

![](img/integration/img_1.png)

The simplest way to add `MimiProfileFragment` is via including in an XML layout.

```xml
<androidx.fragment.app.FragmentContainerView
        android:id="@+id/mimiProfileFragment"
        android:name="io.mimi.sdk.profile.MimiProfileFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center" />
```

You can also dynamically add a `MimiProfileFragment` instance to your layouts through the standard Android `FragmentManager` mechanism.

## Theming

Once you have integrated the UI components, you need to setup the Mimi theme. This is important because without setting it up, your app will crash when attempting to inflate the Mimi UI components.

Now navigate to `AndroidManifest.xml` and check your `<application>` tag. Usually there is a theme already defined. Let's assume it is called `AppTheme`.

```xml
<application
    ...
    android:theme="@style/AppTheme" >
```

Now navigate to where `AppTheme` is defined (should be in `styles.xml` or `theme.xml` file under `res`) and replace value for `parent` with `Theme.Mimi`

```xml
<style name="AppTheme" parent="Theme.Mimi">
```

Now, run your app. Everything should be functional.

## [Read core docs for Theming](https://mimihearingtechnologies.github.io/SDK-Android/latest/theming/)
