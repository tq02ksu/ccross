package com.github.tq02ksu.ccross.audio.m4a;

import org.jflac.sound.spi.Flac2PcmAudioInputStream;
import org.jflac.sound.spi.FlacEncoding;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

public class M4AFormatConversionProvider extends FormatConversionProvider {
    @Override
    public AudioFormat.Encoding[] getSourceEncodings() {
        return new AudioFormat.Encoding[] {ALACEncoding.ALAC};
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings() {
        return new AudioFormat.Encoding[] {ALACEncoding.PCM_SIGNED};
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
        boolean bitSizeOK = this.isBitSizeOK(sourceFormat, true);
        boolean channelsOK = this.isChannelsOK(sourceFormat, true);
        AudioFormat.Encoding[] encodings;
        if (bitSizeOK && channelsOK && sourceFormat.getEncoding().equals(ALACEncoding.ALAC)) {
            encodings = new AudioFormat.Encoding[]{AudioFormat.Encoding.PCM_SIGNED};
            return encodings;
        } else {
            encodings = new AudioFormat.Encoding[0];
            return encodings;
        }
    }

    @Override
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        return this.getTargetFormats(targetEncoding, sourceFormat, true);
    }

    private AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat, boolean notSpecifiedOK) {
        boolean bitSizeOK = this.isBitSizeOK(sourceFormat, notSpecifiedOK);
        boolean channelsOK = this.isChannelsOK(sourceFormat, notSpecifiedOK);
        AudioFormat[] formats;
        if (bitSizeOK && channelsOK && sourceFormat.getEncoding().equals(FlacEncoding.FLAC) && targetEncoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            formats = new AudioFormat[]{new AudioFormat(sourceFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(), sourceFormat.getChannels(), true, false)};
            return formats;
        } else {
            formats = new AudioFormat[0];
            return formats;
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
        AudioFormat[] formats = this.getTargetFormats(targetEncoding, sourceStream.getFormat(), false);
        if (formats.length > 0) {
            return this.getAudioInputStream(formats[0], sourceStream);
        } else {
            throw new IllegalArgumentException("conversion not supported");
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat[] formats = this.getTargetFormats(targetFormat.getEncoding(), sourceFormat, false);
        if (formats.length > 0) {
            if (sourceFormat.equals(targetFormat)) {
                return sourceStream;
            } else if (sourceFormat.getChannels() == targetFormat.getChannels() && sourceFormat.getSampleSizeInBits() == targetFormat.getSampleSizeInBits() && !targetFormat.isBigEndian() && sourceFormat.getEncoding().equals(FlacEncoding.FLAC) && targetFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
                return new ALAC2PcmAudioInputStream(sourceStream, targetFormat, -1L);
            } else if (sourceFormat.getChannels() == targetFormat.getChannels() && sourceFormat.getSampleSizeInBits() == targetFormat.getSampleSizeInBits() && sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && targetFormat.getEncoding().equals(FlacEncoding.FLAC)) {
                throw new IllegalArgumentException("FLAC encoder not yet implemented");
            } else {
                throw new IllegalArgumentException("unable to convert " + sourceFormat.toString() + " to " + targetFormat.toString());
            }
        } else {
            throw new IllegalArgumentException("conversion not supported");
        }
    }

    private boolean isBitSizeOK(AudioFormat format, boolean notSpecifiedOK) {
        int bitSize = format.getSampleSizeInBits();
        return notSpecifiedOK && bitSize == -1 || bitSize == 8 || bitSize == 16 || bitSize == 24;
    }

    private boolean isChannelsOK(AudioFormat format, boolean notSpecifiedOK) {
        int channels = format.getChannels();
        return notSpecifiedOK && channels == -1 || channels == 1 || channels == 2;
    }
}
