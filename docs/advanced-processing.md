# Advanced Processing

## Observing changes in the `ProcessingParameter` state

### Example for observing changes in parameter values

```kotlin
// Observer for a MimiProcessingParameter, for example: isEnabled
activeSession.isEnabled.observe { state ->
    // add your code here to handle changes in the parameter state
}
```

You should call this function usually right after you have activated the processing session. 
i.e For demonstration purpose we will be calling it inside `onCreate()`, right after activating 
processing session as per earlier example:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launchWhenCreated {
            activateProcessingSession()
            
            // Function called after activating processing session
            setupObserver()
        }
    }
```
