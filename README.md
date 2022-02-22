# ExpandableTextView

Light-weighted, convenient implementation of expandable text view that supports expanding & collapsing animations for
Android projects.

## Why ExpandableTextView?

When the text is too long, a designer reasonably asks if it is possible to truncate the text by having a certain line
count limitation. In addition, an expanding call-to-action (ie. "Read more") should be shown at the end of the text.
When user taps the text, it expands to show the full content. ExpandableTextView helps creating such behaviour easily.

## Demonstration

| Normal               | RTL                   | With drawable         |
| :------------------: | :-------------------: | :-------------------: |
| <video src="https://github.com/giangpham96/ExpandableTextView/blob/main/arts/normal.mp4" height="500"> | <video src="https://github.com/giangpham96/ExpandableTextView/blob/main/arts/rtl.mp4" height="500"> | <video src="https://github.com/giangpham96/ExpandableTextView/blob/main/arts/drawable.mp4" height="500"> |


| maxLines when expand  | Property changes at runtime | Width changes at runtime         |
| :-------------------: | :-------------------------: | :------------------------------: |
| <video src="https://github.com/giangpham96/ExpandableTextView/blob/main/arts/maxLines.mp4" height="500"> | <video src="https://github.com/giangpham96/ExpandableTextView/blob/main/arts/properties.mp4" height="500"> | <video src="https://github.com/giangpham96/ExpandableTextView/blob/main/arts/width.mp4" height="500"> |

## Install
TBD

## Usage

### Example XML

```xml
        <la.me.leo.expandabletextview.ExpandableTextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/purple_100"
            android:padding="16dp"
            android:maxLines="10"
            app:expandCta="More"
            app:collapsedMaxLines="2"
            app:expandCtaColor="@color/blue_500"
            app:expandableText="@string/long_text" />
```

### Attributes

| Attributes            | Type                 | Description                 | Default value                    |
| :-------------------: | :-------------------------: | :-------------------------: | :------------------------------: |
| app:collapsedMaxLines | Int | The maximum line counts when the text is collapsed | 3 |
| app:expandCta | String | The Cta at the end of truncated text such as "View more" | "" (Nothing will be shown at the end of truncated text)|
| app:expandCtaColor | Color | The color of expand Cta | #ffaa66cc |
| app:expandableText | String | The text to be displayed on this text view | "" |

### Public functions

- `collapsedMaxLines` public getter & setter for the maximum line counts when the text is collapsed.
- `expandCta` public getter & setter for the Cta at the end of truncated text such as "View more".
- `expandCtaColor` public getter & setter for the color of expand Cta.
- `expandableText` public getter & setter for the text to be displayed on this text view.
- `collapsed` public getter to determine if the text is being collapsed
- `expanded` public getter to determine if the text is being expanded
- `toggle` function that makes the text changes its state from collapsed to expanded & vice versa. It also adds
animation transition during the state change.

### Notes

- **DO NOT** directly use `android:text` or `setText` in this view. Use `app:expandableText` or `expandableText` instead.
Attempting to use `android:text` or `setText` will lead to unexpected behaviour.
- At any time, `collapsedMaxLines` **MUST** always be less than or equal to `maxLines`. Otherwise, an exception will be
thrown.
- This view only supports `TextUtils.TruncateAt.END`.
