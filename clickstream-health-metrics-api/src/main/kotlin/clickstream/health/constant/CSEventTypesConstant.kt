package clickstream.health.constant

import androidx.annotation.RestrictTo


@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object CSEventTypesConstant {
    public const val INSTANT: String = "instant"
    public const val AGGREGATE: String = "aggregate"
    public const val BUCKET: String = "bucket"
}