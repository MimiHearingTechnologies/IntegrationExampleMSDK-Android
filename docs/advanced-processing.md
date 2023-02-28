# Advanced Processing

## Observing changes in the `ProcessingParameter` state

### Example for observing changes in parameter values

```kotlin
private suspend fun setupObserver() {
    // Observer for a MimiProcessingParameter : isEnabled
    activeSession.isEnabled.observe(
        applying = { current, applying ->
            // add your code here to handle when a value application is in progress
        },
        failed = { current, failedToApply, exception ->
            // add your code here to handle when a value application fails
        },
        ready = { current ->
            // add your code here to handle when value application succeeds
        }
    )

    // Observer for a MimiProcessingParameter : intensity
    activeSession.intensity.observe(
        applying = { current, applying ->
            // add your code here to handle when a value application is in progress
        },
        failed = { current, failedToApply, exception ->
            // add your code here to handle when a value application fails
        },
        ready = { current ->
            // add your code here to handle when value application succeeds
        }
    )

    // Observer for a MimiFetchedProcessingParameter : preset
    activeSession.preset.observe(
        fetching = { current ->
            // add your code here to handle when a value application is fetching from the data source
        },
        applying = { current, applying ->
            // add your code here to handle when a value application is in progress
        },
        failed = { current, failedToApply, exception ->
            // add your code here to handle when a value application fails
        },
        ready = { current ->
            // add your code here to handle when value application succeeds
        }
    )
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
