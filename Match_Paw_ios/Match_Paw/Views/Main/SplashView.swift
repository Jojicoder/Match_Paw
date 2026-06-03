//
//  SplashView.swift
//  Match_Paw
//

import SwiftUI

struct SplashView: View {
    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    @State private var toeOffsets: [CGFloat] = Array(repeating: -80, count: 4)
    @State private var toeOpacities: [Double] = Array(repeating: 0, count: 4)
    @State private var padScale: CGFloat = 0
    @State private var textOpacity: Double = 0
    @State private var textOffset: CGFloat = 24

    var onFinish: () -> Void

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            VStack(spacing: 18) {
                pawPrint

                Text("Match Paw")
                    .font(.system(size: 44, weight: .bold, design: .rounded))
                    .foregroundColor(pawBrown)
                    .opacity(textOpacity)
                    .offset(y: textOffset)
            }
        }
        .onAppear(perform: startAnimation)
    }

    private var pawPrint: some View {
        ZStack {
            // Main pad
            Ellipse()
                .frame(width: 108, height: 88)
                .offset(y: 28)
                .scaleEffect(padScale)

            // Left outer toe
            toeBean(index: 0, x: -52, y: -14, w: 34, h: 42)
            // Left inner toe
            toeBean(index: 1, x: -17, y: -44, w: 38, h: 46)
            // Right inner toe
            toeBean(index: 2, x:  17, y: -44, w: 38, h: 46)
            // Right outer toe
            toeBean(index: 3, x:  52, y: -14, w: 34, h: 42)
        }
        .foregroundColor(pawBrown)
        .frame(width: 180, height: 180)
    }

    private func toeBean(index: Int, x: CGFloat, y: CGFloat, w: CGFloat, h: CGFloat) -> some View {
        Ellipse()
            .frame(width: w, height: h)
            .offset(x: x, y: y + toeOffsets[index])
            .opacity(toeOpacities[index])
    }

    private func startAnimation() {
        let toeDelays: [Double] = [0.0, 0.13, 0.26, 0.39]

        for i in 0..<4 {
            withAnimation(.spring(response: 0.38, dampingFraction: 0.52).delay(toeDelays[i])) {
                toeOffsets[i] = 0
                toeOpacities[i] = 1
            }
        }

        withAnimation(.spring(response: 0.5, dampingFraction: 0.55).delay(0.55)) {
            padScale = 1
        }

        withAnimation(.easeOut(duration: 0.4).delay(0.95)) {
            textOpacity = 1
            textOffset = 0
        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            onFinish()
        }
    }
}

#Preview {
    SplashView(onFinish: {})
}
